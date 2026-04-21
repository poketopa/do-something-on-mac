document.addEventListener('DOMContentLoaded', () => {
    // --- DOM Elements ---
    const authContainer = document.getElementById('auth-container');
    const portfolioContainer = document.getElementById('portfolio-container');
    const signupForm = document.getElementById('signup-form');
    const loginForm = document.getElementById('login-form');
    const logoutButton = document.getElementById('logout-btn');
    const portfolioForm = document.getElementById('portfolio-form');
    const assetList = document.getElementById('asset-list');
    const addAssetButton = document.getElementById('add-asset');
    const calculateButton = document.getElementById('calculate-btn');
    const saveButton = document.getElementById('save-btn');
    const resultsDiv = document.getElementById('results');
    const messageDiv = document.getElementById('message-area');
    const helpBtn = document.getElementById('help-btn');
    const helpModal = document.getElementById('help-modal');
    const closeHelp = document.getElementById('close-help');
    const showCashFormBtn = document.getElementById('show-cash-form');
    const cashForm = document.getElementById('cash-form');
    const cashSymbolSelect = document.getElementById('cash-symbol');
    const cashAmountInput2 = document.getElementById('cash-amount');
    const addCashBtn2 = document.getElementById('add-cash-btn');
    const cancelCashBtn = document.getElementById('cancel-cash-btn');

    // --- Preset State ---
    let currentPreset = 1;
    const presetButtons = document.querySelectorAll('.preset-btn');
    function updatePresetUI() {
        presetButtons.forEach(btn => {
            if (parseInt(btn.dataset.preset) === currentPreset) {
                btn.style.backgroundColor = '#2980b9';
                btn.style.color = '#fff';
            } else {
                btn.style.backgroundColor = '';
                btn.style.color = '';
            }
        });
    }
    presetButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            currentPreset = parseInt(btn.dataset.preset);
            updatePresetUI();
            loadPortfolio();
        });
    });
    updatePresetUI();

    // --- State Management ---
    let token = localStorage.getItem('accessToken');

    const updateUI = () => {
        if (token) {
            authContainer.style.display = 'none';
            portfolioContainer.style.display = 'block';
            loadPortfolio();
        } else {
            authContainer.style.display = 'block';
            portfolioContainer.style.display = 'none';
        }
    };

    const showMessage = (message, isError = false) => {
        messageDiv.textContent = message;
        messageDiv.style.color = isError ? 'red' : 'green';
        setTimeout(() => messageDiv.textContent = '', 3000);
    };

    const apiFetch = async (url, options = {}) => {
        const headers = {
            'Content-Type': 'application/json',
            ...options.headers,
        };
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const response = await fetch(url, { ...options, headers });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ detail: 'An unknown error occurred' }));
            throw new Error(errorData.detail || 'Request failed');
        }
        
        // For 204 No Content
        if (response.status === 204) {
            return null;
        }

        return response.json();
    };

    // --- Event Listeners ---
    signupForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const username = e.target.username.value;
        const password = e.target.password.value;
        try {
            await apiFetch('/api/users/signup', {
                method: 'POST',
                body: JSON.stringify({ username, password }),
            });
            showMessage('Signup successful! Please log in.');
            signupForm.reset();
        } catch (error) {
            showMessage(error.message, true);
        }
    });

    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const formData = new FormData(e.target);
        try {
            const data = await fetch('/api/token', {
                method: 'POST',
                body: formData,
            }).then(res => res.json());

            if (data.access_token) {
                token = data.access_token;
                localStorage.setItem('accessToken', token);
                updateUI();
            } else {
                throw new Error(data.detail || 'Login failed');
            }
        } catch (error) {
            showMessage(error.message, true);
        }
    });

    logoutButton.addEventListener('click', () => {
        token = null;
        localStorage.removeItem('accessToken');
        updateUI();
        // Clear portfolio display
        assetList.innerHTML = '';
        resultsDiv.innerHTML = '';
    });
    
    addAssetButton.addEventListener('click', () => {
        addAssetRow();
    });

    saveButton.addEventListener('click', async () => {
        const assets = getAssetsFromForm();
        try {
            await apiFetch(`/api/portfolio?preset=${currentPreset}`, {
                method: 'POST',
                body: JSON.stringify({ assets }),
            });
            showMessage('포트폴리오 저장 완료!');
        } catch (error) {
            showMessage(error.message, true);
        }
    });

    calculateButton.addEventListener('click', async () => {
        resultsDiv.innerHTML = '<p>Calculating...</p>';
        try {
            const data = await apiFetch(`/api/portfolio/value?preset=${currentPreset}`);
            if (data.success) {
                displayResults(data);
            } else {
                throw new Error(data.error || 'Failed to calculate portfolio value.');
            }
        } catch (error) {
            showMessage(error.message, true);
            resultsDiv.innerHTML = '';
        }
    });

    // 도움말 모달 기능
    if (helpBtn && helpModal && closeHelp) {
        helpBtn.onclick = () => { helpModal.style.display = 'flex'; };
        closeHelp.onclick = () => { helpModal.style.display = 'none'; };
        helpModal.onclick = (e) => {
            if (e.target === helpModal) helpModal.style.display = 'none';
        };
    }

    // 현금 추가 폼 제어 및 추가 기능
    if (showCashFormBtn && cashForm && cashSymbolSelect && cashAmountInput2 && addCashBtn2 && cancelCashBtn) {
        showCashFormBtn.onclick = () => {
            cashForm.style.display = 'block';
            cashAmountInput2.value = '';
        };
        cancelCashBtn.onclick = () => {
            cashForm.style.display = 'none';
        };
        addCashBtn2.onclick = () => {
            const symbol = cashSymbolSelect.value;
            const quantity = parseFloat(cashAmountInput2.value);
            if (!quantity || quantity <= 0) return;
            addAssetRow(symbol, quantity, 'cash', '');
            cashForm.style.display = 'none';
            cashAmountInput2.value = '';
        };
    }

    // --- Helper Functions ---
    const addAssetRow = (symbol = '', quantity = '', screener = '', exchange = '') => {
        const row = document.createElement('div');
        row.className = 'asset-item';
        row.innerHTML = `
            <input type="text" class="asset-symbol" placeholder="심볼" value="${symbol}" required>
            <input type="number" class="asset-quantity" placeholder="수량" value="${quantity}" step="any" min="0" required>
            <input type="text" class="asset-screener" placeholder="종류" value="${screener}" required>
            <input type="text" class="asset-exchange" placeholder="거래소" value="${exchange}" required>
            <button type="button" class="remove-asset-btn">삭제</button>
        `;
        assetList.appendChild(row);
        row.querySelector('.remove-asset-btn').addEventListener('click', () => row.remove());
    };

    const getAssetsFromForm = () => {
        const assets = [];
        const assetItems = assetList.querySelectorAll('.asset-item');
        assetItems.forEach(item => {
            const symbol = item.querySelector('.asset-symbol').value.trim().toUpperCase();
            const quantity = parseFloat(item.querySelector('.asset-quantity').value);
            const screener = item.querySelector('.asset-screener').value.trim();
            const exchange = item.querySelector('.asset-exchange').value.trim();
            if (symbol && quantity > 0 && screener) {
                assets.push({ symbol, quantity, screener, exchange });
            }
        });
        return assets;
    };

    const loadPortfolio = async () => {
        try {
            const assets = await apiFetch(`/api/portfolio?preset=${currentPreset}`);
            assetList.innerHTML = ''; // Clear existing list
            if (assets && assets.length > 0) {
                assets.forEach(asset => addAssetRow(asset.symbol, asset.quantity, asset.screener, asset.exchange));
            } else {
                // Add one empty row if portfolio is empty
                addAssetRow();
            }
        } catch (error) {
            showMessage(error.message, true);
            if (error.message.includes('Could not validate credentials')) {
                logoutButton.click();
            }
        }
    };

    // 숫자 포맷팅 함수 (쉼표 포함)
    const formatNumber = (num, digits = 0) => {
        if (isNaN(num)) return '-';
        return num.toLocaleString('ko-KR', { maximumFractionDigits: digits });
    };

    const displayResults = (data) => {
        let html = `
            <h3>계산 결과</h3>
            <p><strong>총 평가액 (USD):</strong> $${formatNumber(data.total_usd)}</p>
            <p><strong>총 평가액 (KRW):</strong> ₩${formatNumber(data.total_krw)}</p>
            <p><strong>USD/KRW 환율:</strong> ${formatNumber(data.usd_krw_rate)}</p>
            <p><strong>USDT/KRW (김치 프리미엄):</strong> ${formatNumber(data.usdt_krw_rate)}</p>
            <hr>
            <h4>자산별 상세 내역:</h4>
        `;
        data.portfolio.forEach(asset => {
            html += `
                <div class="asset-result">
                    <strong>${asset.symbol}</strong>
                    <p>수량: ${formatNumber(asset.quantity, 2)}</p>
                    <p>USD 가격: $${formatNumber(asset.usd_price)}</p>
                    <p>KRW 가격: ₩${formatNumber(asset.krw_price)}</p>
                    <p>USD 평가액: $${formatNumber(asset.total_usd)}</p>
                    <p>KRW 평가액: ₩${formatNumber(asset.total_krw)}</p>
                </div>
            `;
        });
        resultsDiv.innerHTML = html;
    };

    // --- Initial Load ---
    updateUI();
});
