window.addEventListener('DOMContentLoaded', event => {
    const signupForm = document.getElementById('signup-form');
    const usernameInput = document.getElementById('username');
    const checkButton = document.getElementById('btn-check-username');
    const usernameCheckMsg = document.getElementById('username-check-msg');

    let isUsernameCheckedAndAvailable = false;
    const passwordInput = document.getElementById('password');
    const passwordConfirmInput = document.getElementById('passwordConfirm');
    const passwordConfirmJsMsg = document.getElementById('password-confirm-js-msg');

    if (signupForm && usernameInput && checkButton) {
        checkButton.addEventListener('click', async () => {
            const username = usernameInput.value;

            usernameCheckMsg.textContent = '';
            usernameCheckMsg.classList.remove('text-success', 'text-danger');
            isUsernameCheckedAndAvailable = false;

            if (username.length < 4) {
                usernameCheckMsg.textContent = '아이디는 4자 이상 20자 이하로 입력해주세요.';
                usernameCheckMsg.classList.add('text-danger');
                return;
            }

            try {
                const response = await fetch(`/members/check-username?username=${username}`);
                if (!response.ok) throw new Error('서버 응답 오류');

                const data = await response.json();

                if (data.available) {
                    usernameCheckMsg.textContent = '사용 가능한 아이디입니다.';
                    usernameCheckMsg.classList.add('text-success');
                    isUsernameCheckedAndAvailable = true;
                } else if (data.invalid) {
                    usernameCheckMsg.textContent = '아이디는 4자 이상 20자 이하로 입력해주세요.';
                    usernameCheckMsg.classList.add('text-danger');
                } else {
                    usernameCheckMsg.textContent = '이 아이디는 이미 사용 중입니다.';
                    usernameCheckMsg.classList.add('text-danger');
                }
            } catch (error) {
                console.error('아이디 중복 검사 중 오류:', error);
                usernameCheckMsg.textContent = '오류가 발생했습니다. 다시 시도해주세요.';
                usernameCheckMsg.classList.add('text-danger');
            }
        });

        usernameInput.addEventListener('input', () => {
            isUsernameCheckedAndAvailable = false;
            usernameCheckMsg.textContent = '';
            usernameCheckMsg.classList.remove('text-success', 'text-danger');
        });

        signupForm.addEventListener('submit', (e) => {
            let isValid = true;

            if (!isUsernameCheckedAndAvailable) {
                e.preventDefault();
                usernameCheckMsg.textContent = '아이디 중복 확인을 먼저 완료해주세요.';
                usernameCheckMsg.classList.add('text-danger');
                if(isValid) usernameInput.focus();
                isValid = false;
            }

            passwordConfirmJsMsg.textContent = '';
            passwordConfirmJsMsg.classList.remove('text-danger');

            if (passwordInput.value !== passwordConfirmInput.value) {
                e.preventDefault();

                passwordConfirmJsMsg.textContent = '비밀번호가 일치하지 않습니다.';
                passwordConfirmJsMsg.classList.add('text-danger');

                if(isValid) passwordConfirmInput.focus();
                isValid = false;
            }
        });

        if (passwordConfirmInput) {
            passwordConfirmInput.addEventListener('input', () => {
                passwordConfirmJsMsg.textContent = '';
                passwordConfirmJsMsg.classList.remove('text-danger');
            });
        }
    }
});