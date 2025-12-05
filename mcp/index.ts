import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { z } from "zod";
import axios from "axios";
import path from "path";
import dotenv from "dotenv";

const api = axios.create({
    baseURL: "http://localhost:8080",
    withCredentials: true,
});

dotenv.config({ path: path.resolve(__dirname, '../../.env') });
const email = process.env.ADMIN_DEFAULT_EMAIL || 'error@test.com';
const password = process.env.ADMIN_DEFAULT_PASSWORD || 'error';

let cookie = "";

async function loginAsAdmin() {
    try {
        const params = new URLSearchParams();
        params.append('email', email);
        params.append('password', password);

        const response = await api.post('/members/login', params);
        const setCookie = response.headers['set-cookie'];
        if (setCookie) {
            cookie = setCookie.join('; ');
            api.defaults.headers.Cookie = cookie;
            console.log("Admin login successful");
        }
    } catch (error) {
        console.error("[Error] Admin login failed:", error);
    }
}

const server = new McpServer({
    name: "munsiksa-admin-server",
    version: "1.0.0",
});

server.tool(
    "search_members",
    "회원 목록을 조회합니다. 특정 회원을 찾거나 전체 목록을 볼 때 사용합니다.",
    {
        page: z.number().default(0).describe("페이지 번호 (0부터 시작)"),
    },
    async ({ page }) => {
        try {
            const response = await api.get(`/admin/members/list?page=${page}`);
            const members = response.data.content.map((m: any) => ({
                id: m.id,
                name: m.name,
                email: m.email,
                points: m.points,
                role: m.role
            }));

            return {
                content: [{ type: "text", text: JSON.stringify(members, null, 2) }],
            };
        } catch (error) {
            return { content: [{ type: "text", text: "회원 목록 조회 실패" }] };
        }
    }
);

server.tool(
    "manage_points",
    "특정 회원에게 포인트를 지급하거나 회수합니다.",
    {
        memberId: z.number().describe("회원 ID (memberId)"),
        amount: z.number().describe("지급할 포인트 양 (회수는 음수 입력)"),
    },
    async ({ memberId, amount }) => {
        try {
            const response = await api.post(`/admin/members/${memberId}/points`, {
                point: amount
            });

            return {
                content: [{ type: "text", text: `성공: ${response.data.message}` }],
            };
        } catch (error) {
            return { content: [{ type: "text", text: "포인트 수정 실패" }] };
        }
    }
);

async function main() {
    await loginAsAdmin();
    const transport = new StdioServerTransport();
    await server.connect(transport);
}

main();