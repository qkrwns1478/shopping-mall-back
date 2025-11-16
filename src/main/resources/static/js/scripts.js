window.addEventListener('DOMContentLoaded', event => {
    const signupForm = document.getElementById('signup-form');
    const emailInput = document.getElementById('email');
    const checkButton = document.getElementById('btn-check-email');
    const emailCheckMsg = document.getElementById('email-check-msg');

    let isEmailCheckedAndAvailable = false;
    const passwordInput = document.getElementById('password');
    const passwordConfirmInput = document.getElementById('passwordConfirm');
    const passwordConfirmJsMsg = document.getElementById('password-confirm-js-msg');

    if (signupForm && emailInput && checkButton) {
        checkButton.addEventListener('click', async () => {
            const email = emailInput.value;

            emailCheckMsg.textContent = '';
            emailCheckMsg.classList.remove('text-success', 'text-danger');
            isEmailCheckedAndAvailable = false;

            if (email.trim() === '' || !email.includes('@')) {
                emailCheckMsg.textContent = '올바른 이메일 형식이 아닙니다.';
                emailCheckMsg.classList.add('text-danger');
                return;
            }

            try {
                const response = await fetch(`/members/check-email?email=${email}`);
                if (!response.ok) throw new Error('서버 응답 오류');

                const data = await response.json();

                if (data.available) {
                    emailCheckMsg.textContent = '사용 가능한 이메일입니다.';
                    emailCheckMsg.classList.add('text-success');
                    isEmailCheckedAndAvailable = true;
                } else if (data.invalid) {
                    emailCheckMsg.textContent = '올바른 이메일 형식이 아닙니다.';
                    emailCheckMsg.classList.add('text-danger');
                } else {
                    emailCheckMsg.textContent = '이 이메일은 이미 사용 중입니다.';
                    emailCheckMsg.classList.add('text-danger');
                }
            } catch (error) {
                console.error('이메일 중복 검사 중 오류:', error);
                emailCheckMsg.textContent = '오류가 발생했습니다. 다시 시도해주세요.';
                emailCheckMsg.classList.add('text-danger');
            }
        });

        emailInput.addEventListener('input', () => {
            isEmailCheckedAndAvailable = false;
            emailCheckMsg.textContent = '';
            emailCheckMsg.classList.remove('text-success', 'text-danger');
        });

        signupForm.addEventListener('submit', (e) => {
            let isValid = true;

            if (!isEmailCheckedAndAvailable) {
                e.preventDefault();
                emailCheckMsg.textContent = '이메일 중복 확인을 먼저 완료해주세요.';
                emailCheckMsg.classList.add('text-danger');
                if(isValid) emailInput.focus();
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