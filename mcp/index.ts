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

// ---------------------------------------------------------
// [회원 관리 도구]
// ---------------------------------------------------------

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

server.tool(
    "add_test_member",
    "테스트용 회원을 생성합니다. 이메일, 비밀번호, 이름, 주소를 필수로 입력받습니다.",
    {
        email: z.string().describe("회원 이메일"),
        password: z.string().describe("회원 비밀번호"),
        name: z.string().describe("회원 이름"),
        address: z.string().describe("회원 주소"),
    },
    async ({ email, password, name, address }) => {
        try {
            const response = await api.post('/admin/members', {
                email,
                password,
                passwordConfirm: password,
                name,
                address,
            });
            return {
                content: [{ type: "text", text: `성공: ${response.data.message}` }],
            };
        } catch (error: any) {
            const msg = error.response?.data?.message || "회원 생성 실패";
            return { content: [{ type: "text", text: `에러: ${msg}` }] };
        }
    }
);

server.tool(
    "delete_member",
    "회원을 삭제합니다. `search_members`로 회원 ID를 확인할 수 있습니다.",
    {
        memberId: z.number().describe("삭제할 회원 ID"),
    },
    async ({ memberId }) => {
        try {
            const response = await api.delete(`/admin/members/${memberId}`);
            return {
                content: [{ type: "text", text: `성공: ${response.data.message}` }],
            };
        } catch (error: any) {
            const msg = error.response?.data?.message || "회원 삭제 실패";
            return { content: [{ type: "text", text: `에러: ${msg}` }] };
        }
    }
);

// ---------------------------------------------------------
// [메인 페이지 진열 관리 도구]
// ---------------------------------------------------------

server.tool(
    "list_items",
    "최근 등록된 상품 목록을 조회합니다. 재고(stockNumber), 가격(price), 판매상태(status) 등을 확인할 수 있습니다.",
    {
        page: z.number().default(0).describe("페이지 번호 (0부터 시작)"),
    },
    async ({ page }) => {
        try {
            const response = await api.get(`/admin/item/list?page=${page}`);
            const items = response.data.content.map((i: any) => ({
                id: i.id,
                name: i.itemNm,
                price: i.price,
                stock: i.stockNumber,
                status: i.itemSellStatus,
                regTime: i.regTime
            }));

            return {
                content: [{ type: "text", text: JSON.stringify(items, null, 2) }],
            };
        } catch (error) {
            return { content: [{ type: "text", text: "상품 목록 조회 실패" }] };
        }
    }
);

server.tool(
    "list_main_items",
    "현재 메인 페이지에 진열된 상품 목록을 조회합니다. 상품의 메인 진열을 해제하려면 여기서 'id(메인아이템ID)'를 확인해야 합니다.",
    {},
    async () => {
        try {
            const response = await api.get('/api/main/items');
            const items = response.data.map((m: any) => ({
                mainItemId: m.id,
                itemId: m.itemId,
                name: m.itemNm,
                price: m.price,
                discountRate: m.discountRate
            }));
            return {
                content: [{ type: "text", text: JSON.stringify(items, null, 2) }],
            };
        } catch (error) {
            return { content: [{ type: "text", text: "메인 상품 목록 조회 실패" }] };
        }
    }
);

server.tool(
    "remove_main_item",
    "메인 페이지에 등록된 상품을 제거(진열 해제)합니다. list_main_items로 확인한 'mainItemId'를 사용해야 합니다.",
    {
        mainItemId: z.number().describe("삭제할 메인 아이템 ID (상품 ID 아님)"),
    },
    async ({ mainItemId }) => {
        try {
            const response = await api.delete(`/admin/main/items/${mainItemId}`);
            return {
                content: [{ type: "text", text: `성공: ${response.data.message}` }],
            };
        } catch (error: any) {
            const msg = error.response?.data?.message || "삭제 실패";
            return { content: [{ type: "text", text: `에러: ${msg}` }] };
        }
    }
);

server.tool(
    "add_main_item",
    "특정 상품을 메인 페이지 추천 상품으로 등록합니다.",
    {
        itemId: z.number().describe("메인에 등록할 원본 상품 ID"),
    },
    async ({ itemId }) => {
        try {
            const response = await api.post('/admin/main/items', { itemId });
            return {
                content: [{ type: "text", text: `성공: ${response.data.message}` }],
            };
        } catch (error: any) {
            const msg = error.response?.data?.message || "등록 실패";
            return { content: [{ type: "text", text: `에러: ${msg}` }] };
        }
    }
);

// ---------------------------------------------------------
// [상품 관리 도구]
// ---------------------------------------------------------

server.tool(
    "add_test_item",
    "테스트용 상품을 생성합니다. 상품명, 가격, 상세설명, 재고, 카테고리ID, 배송비를 필수로 입력받습니다. `list_categories`로 카테고리 ID를 확인할 수 있습니다.",
    {
        itemNm: z.string().describe("상품명"),
        price: z.number().describe("가격"),
        itemDetail: z.string().describe("상품 상세 설명"),
        stockNumber: z.number().describe("재고 수량"),
        categoryId: z.number().describe("카테고리 ID"),
        deliveryFee: z.number().describe("배송비 (무료는 0)"),
        // Optional fields
        itemSellStatus: z.enum(["SELL", "SOLD_OUT"]).default("SELL").describe("판매상태 (SELL, SOLD_OUT)"),
        brand: z.string().optional().describe("브랜드"),
        origin: z.string().optional().describe("원산지"),
    },
    async (params) => {
        try {
            const response = await api.post('/admin/item/new', {
                ...params,
                itemSellStatus: params.itemSellStatus || 'SELL',
                imgUrlList: [],
                itemOptionList: [],
                discount: false,
                discountRate: 0,
                payback: false,
            });
            return {
                content: [{ type: "text", text: `성공: ${response.data.message}` }],
            };
        } catch (error: any) {
            const msg = error.response?.data?.message || "상품 생성 실패";
            return { content: [{ type: "text", text: `에러: ${msg}` }] };
        }
    }
);

server.tool(
    "delete_item",
    "상품을 삭제합니다. `list_items`로 상품 ID를 확인할 수 있습니다.",
    {
        itemId: z.number().describe("삭제할 상품 ID"),
    },
    async ({ itemId }) => {
        try {
            const response = await api.delete(`/admin/item/${itemId}`);
            return {
                content: [{ type: "text", text: `성공: ${response.data.message}` }],
            };
        } catch (error: any) {
            const msg = error.response?.data?.message || "상품 삭제 실패";
            return { content: [{ type: "text", text: `에러: ${msg}` }] };
        }
    }
);

server.tool(
    "update_item",
    "상품 정보를 수정합니다. `list_items`로 상품 ID를 확인할 수 있습니다. 모든 파라미터는 선택사항입니다.",
    {
        itemId: z.number().describe("수정할 상품 ID"),
        itemNm: z.string().optional().describe("새 상품명"),
        price: z.number().optional().describe("새 가격"),
        itemDetail: z.string().optional().describe("새 상품 상세 설명"),
        stockNumber: z.number().optional().describe("새 재고 수량"),
        itemSellStatus: z.enum(["SELL", "SOLD_OUT"]).optional().describe("새 판매상태"),
        categoryId: z.number().optional().describe("새 카테고리 ID"),
        deliveryFee: z.number().optional().describe("새 배송비"),
    },
    async (params) => {
        try {
            const currentItemRes = await api.get(`/admin/item/${params.itemId}`);
            if (!currentItemRes.data.success) {
                return { content: [{ type: "text", text: `에러: 상품 정보를 가져올 수 없습니다. (${currentItemRes.data.message})` }] };
            }
            const currentItem = currentItemRes.data.data;

            const updatedItemDto = {
                ...currentItem,
                ...params,
            };

            const response = await api.post(`/admin/item/${params.itemId}`, updatedItemDto);

            return {
                content: [{ type: "text", text: `성공: ${response.data.message}` }],
            };
        } catch (error: any) {
            const msg = error.response?.data?.message || "상품 수정 실패";
            return { content: [{ type: "text", text: `에러: ${msg}` }] };
        }
    }
);


// ---------------------------------------------------------
// [카테고리 관리 도구]
// ---------------------------------------------------------

server.tool(
    "list_categories",
    "현재 등록된 카테고리 전체 목록을 조회합니다.",
    {},
    async () => {
        try {
            const response = await api.get('/api/categories');
            return {
                content: [{ type: "text", text: JSON.stringify(response.data, null, 2) }],
            };
        } catch (error) {
            return { content: [{ type: "text", text: "카테고리 조회 실패" }] };
        }
    }
);

server.tool(
    "add_category",
    "새로운 카테고리를 추가합니다.",
    {
        name: z.string().describe("추가할 카테고리 이름"),
    },
    async ({ name }) => {
        try {
            const response = await api.post('/api/categories', { name });
            return {
                content: [{ type: "text", text: `성공: ${response.data.message}` }],
            };
        } catch (error: any) {
            const msg = error.response?.data?.message || "추가 실패";
            return { content: [{ type: "text", text: `에러: ${msg}` }] };
        }
    }
);

server.tool(
    "delete_category",
    "기존 카테고리를 삭제합니다. 해당 카테고리에 속한 상품이 있다면 삭제되지 않을 수 있습니다.",
    {
        categoryId: z.number().describe("삭제할 카테고리 ID"),
    },
    async ({ categoryId }) => {
        try {
            const response = await api.delete(`/api/categories/${categoryId}`);
            return {
                content: [{ type: "text", text: `성공: ${response.data.message}` }],
            };
        } catch (error: any) {
            const msg = error.response?.data?.message || "삭제 실패";
            return { content: [{ type: "text", text: `에러: ${msg}` }] };
        }
    }
);

// ---------------------------------------------------------
// [주문 관리 도구]
// ---------------------------------------------------------

server.tool(
    "search_orders",
    "관리자용 주문 목록을 조회합니다. 주문번호, 주문자(이메일), 상태, 결제금액, 대표 상품명 등을 확인할 수 있습니다.",
    {
        page: z.number().default(0).describe("페이지 번호 (0부터 시작)"),
    },
    async ({ page }) => {
        try {
            const response = await api.get(`/api/admin/orders?page=${page}`);

            const orders = response.data.content.map((o: any) => ({
                orderId: o.orderId,
                member: o.memberEmail,
                date: o.orderDate,
                status: o.orderStatus,
                amount: o.totalAmount,
                items: o.orderItemDtoList.map((item: any) =>
                    `${item.itemNm} (${item.count}개)`
                ).join(', ')
            }));

            return {
                content: [{ type: "text", text: JSON.stringify(orders, null, 2) }],
            };
        } catch (error) {
            return { content: [{ type: "text", text: "주문 목록 조회 실패" }] };
        }
    }
);

server.tool(
    "cancel_order",
    "특정 주문을 취소 처리합니다. (주의: DB 상태만 변경되며, 실제 PG사 결제 취소는 포트원 관리자 콘솔에서 진행해야 한다는 메시지를 반환합니다)",
    {
        orderId: z.number().describe("취소할 주문 ID"),
    },
    async ({ orderId }) => {
        try {
            const response = await api.post(`/api/admin/orders/${orderId}/cancel`);
            return {
                content: [{ type: "text", text: `성공: ${response.data.message}` }],
            };
        } catch (error: any) {
            const msg = error.response?.data?.message || "주문 취소 실패";
            return { content: [{ type: "text", text: `에러: ${msg}` }] };
        }
    }
);

// ---------------------------------------------------------
// [쿠폰 관리 도구]
// ---------------------------------------------------------

server.tool(
    "list_coupons",
    "현재 등록된 모든 쿠폰 목록을 조회합니다.",
    {},
    async () => {
        try {
            const response = await api.get('/admin/coupons');
            const coupons = response.data.map((c: any) => ({
                id: c.id,
                name: c.name,
                code: c.code,
                discountAmount: c.discountAmount,
                validUntil: c.validUntil
            }));

            return {
                content: [{ type: "text", text: JSON.stringify(coupons, null, 2) }],
            };
        } catch (error) {
            return { content: [{ type: "text", text: "쿠폰 목록 조회 실패" }] };
        }
    }
);

server.tool(
    "create_coupon",
    "새로운 쿠폰을 생성합니다. 유효기간은 'YYYY-MM-DD' 형식으로 입력합니다.",
    {
        name: z.string().describe("쿠폰 이름"),
        code: z.string().describe("쿠폰 코드 (중복 불가)"),
        discountAmount: z.number().describe("할인 금액"),
        validUntil: z.string().describe("유효기간 (예: 2024-12-31)"),
    },
    async (params) => {
        try {
            const response = await api.post('/admin/coupons', params);
            return {
                content: [{ type: "text", text: `성공: ${response.data.message}` }],
            };
        } catch (error: any) {
            const msg = error.response?.data?.message || "쿠폰 생성 실패";
            return { content: [{ type: "text", text: `에러: ${msg}` }] };
        }
    }
);

server.tool(
    "bulk_issue_coupon",
    "특정 쿠폰을 선택한 회원들에게 일괄 지급합니다.",
    {
        couponId: z.number().describe("지급할 쿠폰 ID"),
        memberIds: z.array(z.number()).describe("지급 대상 회원 ID 목록 (예: [1, 2, 3])"),
    },
    async ({ couponId, memberIds }) => {
        try {
            const response = await api.post('/admin/coupons/bulk-issue', {
                couponId,
                memberIds
            });
            return {
                content: [{ type: "text", text: `성공: ${response.data.message}` }],
            };
        } catch (error: any) {
            const msg = error.response?.data?.message || "쿠폰 일괄 지급 실패";
            return { content: [{ type: "text", text: `에러: ${msg}` }] };
        }
    }
);

async function main() {
    await loginAsAdmin();
    const transport = new StdioServerTransport();
    await server.connect(transport);
}

main();