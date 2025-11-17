window.addEventListener('DOMContentLoaded', event => {
    const signupForm = document.getElementById('signup-form');

    const csrfToken = document.querySelector('input[name="_csrf"]');

    const emailInput = document.getElementById('email');
    const sendCodeButton = document.getElementById('btn-send-code');
    const emailMsg = document.getElementById('email-msg');

    const codeGroup = document.getElementById('verification-code-group');
    const codeInput = document.getElementById('verification-code');
    const verifyCodeButton = document.getElementById('btn-verify-code');
    const codeMsg = document.getElementById('code-msg');

    const passwordInput = document.getElementById('password');
    const passwordConfirmInput = document.getElementById('passwordConfirm');
    const passwordConfirmJsMsg = document.getElementById('password-confirm-js-msg');

    const searchAddressButton = document.getElementById('btn-search-address');
    const postcode = document.getElementById('postcode');
    const mainAddress = document.getElementById('mainAddress');
    const detailAddress = document.getElementById('detailAddress');
    const fullAddressInput = document.getElementById('fullAddressInput');

    let isEmailVerified = false;
    let timerInterval = null;
    const codeValidTime = 300; // 5분

    function stopTimer() {
        if (timerInterval) {
            clearInterval(timerInterval);
            timerInterval = null;
        }
    }

    function startTimer() {
        stopTimer();
        let remainingTime = codeValidTime;

        const updateTimerDisplay = () => {
            const minutes = Math.floor(remainingTime / 60).toString().padStart(2, '0');
            const seconds = (remainingTime % 60).toString().padStart(2, '0');
            emailMsg.textContent = `인증번호가 발송되었습니다. (유효시간: ${minutes}:${seconds})`;
            emailMsg.classList.remove('text-danger');
            emailMsg.classList.add('text-success');

            remainingTime--;

            if (remainingTime < 0) {
                stopTimer();
                emailMsg.textContent = '인증번호 유효시간이 만료되었습니다. 다시 시도해주세요.';
                emailMsg.classList.remove('text-success');
                emailMsg.classList.add('text-danger');

                emailInput.readOnly = false;
                sendCodeButton.disabled = false;
                sendCodeButton.innerHTML = '인증번호 발송';
                codeGroup.style.display = 'none';

                codeInput.value = '';
                codeMsg.textContent = '';
            }
        };

        updateTimerDisplay();
        timerInterval = setInterval(updateTimerDisplay, 1000);
    }

    function updateFullAddress() {
        if (!postcode || !mainAddress || !detailAddress || !fullAddressInput) return;

        const post = postcode.value.trim();
        const main = mainAddress.value.trim();
        const detail = detailAddress.value.trim();

        if (main) {
            let fullAddr = `(${post}) ${main}`;
            if (detail) {
                fullAddr += `, ${detail}`;
            }
            fullAddressInput.value = fullAddr;
        } else {
            fullAddressInput.value = '';
        }
    }

    if (signupForm && emailInput && sendCodeButton && codeGroup) {

        sendCodeButton.addEventListener('click', async () => {
            const email = emailInput.value;

            emailMsg.textContent = '';
            emailMsg.classList.remove('text-success', 'text-danger');
            isEmailVerified = false;

            if (email.trim() === '' || !email.includes('@')) {
                emailMsg.textContent = '올바른 이메일 형식이 아닙니다.';
                emailMsg.classList.add('text-danger');
                return;
            }

            sendCodeButton.disabled = true;
            sendCodeButton.innerHTML = `
                <span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
                전송 중...
            `;

            try {
                const response = await fetch('/members/send-verification-email', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                        'X-CSRF-TOKEN': csrfToken ? csrfToken.value : ''
                    },
                    body: `email=${encodeURIComponent(email)}`
                });

                if (!response.ok) throw new Error('서버 응답 오류');
                const data = await response.json();

                if (data.success) {
                    codeGroup.style.display = 'block';
                    emailInput.readOnly = true;
                    startTimer();
                } else {
                    emailMsg.textContent = data.message;
                    emailMsg.classList.add('text-danger');
                    sendCodeButton.disabled = false;
                }
            } catch (error) {
                console.error('이메일 인증 발송 중 오류:', error);
                emailMsg.textContent = '오류가 발생했습니다. 다시 시도해주세요.';
                emailMsg.classList.add('text-danger');
                sendCodeButton.disabled = false;
            } finally {
                sendCodeButton.innerHTML = '인증번호 발송';
            }
        });

        verifyCodeButton.addEventListener('click', async () => {
            const email = emailInput.value;
            const code = codeInput.value;

            codeMsg.textContent = '';
            codeMsg.classList.remove('text-success', 'text-danger');

            if (code.trim().length !== 6) {
                codeMsg.textContent = '6자리 인증번호를 입력하세요.';
                codeMsg.classList.add('text-danger');
                return;
            }

            try {
                const response = await fetch('/members/verify-code', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                        'X-CSRF-TOKEN': csrfToken ? csrfToken.value : ''
                    },
                    body: `email=${encodeURIComponent(email)}&code=${encodeURIComponent(code)}`
                });

                if (!response.ok) throw new Error('서버 응답 오류');
                const data = await response.json();

                if (data.verified) {
                    stopTimer();
                    codeMsg.textContent = '이메일 인증에 성공했습니다.';
                    codeMsg.classList.add('text-success');
                    isEmailVerified = true;
                    codeInput.disabled = true;
                    verifyCodeButton.disabled = true;

                    emailMsg.textContent = '이메일 인증이 완료되었습니다.';
                    emailMsg.classList.add('text-success');
                } else {
                    codeMsg.textContent = '인증번호가 일치하지 않습니다.';
                    codeMsg.classList.add('text-danger');
                }
            } catch (error) {
                console.error('인증번호 확인 중 오류:', error);
                codeMsg.textContent = '오류가 발생했습니다. 다시 시도해주세요.';
                codeMsg.classList.add('text-danger');
            }
        });

        emailInput.addEventListener('input', () => {
            stopTimer();
            isEmailVerified = false;
            emailMsg.textContent = '';
            emailMsg.classList.remove('text-success', 'text-danger');
            codeGroup.style.display = 'none';
            codeInput.value = '';
            codeMsg.textContent = '';
            emailInput.readOnly = false;
            sendCodeButton.innerHTML = '인증번호 발송';
        });

        if (searchAddressButton) {
            searchAddressButton.addEventListener('click', () => {
                new daum.Postcode({
                    oncomplete: function(data) {
                        postcode.value = data.zonecode;
                        mainAddress.value = data.roadAddress || data.jibunAddress;

                        detailAddress.focus();
                        updateFullAddress();
                    }
                }).open();
            });
        }

        if (detailAddress) {
            detailAddress.addEventListener('input', updateFullAddress);
        }

        signupForm.addEventListener('submit', (e) => {
            let isValid = true;

            if (!isEmailVerified) {
                e.preventDefault();
                emailMsg.textContent = '이메일 인증을 완료해주세요.';
                emailMsg.classList.remove('text-success');
                emailMsg.classList.add('text-danger');
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

            updateFullAddress();
        });

        if (passwordConfirmInput) {
            passwordConfirmInput.addEventListener('input', () => {
                passwordConfirmJsMsg.textContent = '';
                passwordConfirmJsMsg.classList.remove('text-danger');
            });
        }
    }
});