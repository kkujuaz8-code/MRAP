let globalToken = "";
let myChart = null; 
let tvWidget = null; 

function showToast(message, isSuccess = true) {
    const toastEl = document.getElementById('liveToast');
    const toastHeader = document.getElementById('toastHeader');
    const toastBody = document.getElementById('toastBody');
    
    toastHeader.className = isSuccess ? 'toast-header text-white bg-success' : 'toast-header text-white bg-danger';
    toastBody.innerText = message;
    
    const toast = new bootstrap.Toast(toastEl);
    toast.show();
}

async function login() {
    const loginId = document.getElementById('loginId').value;
    const password = document.getElementById('password').value;
    
    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ loginId, password })
        });
        
        if (response.ok) {
            const data = await response.json();
            globalToken = data.token;
            
            document.getElementById('loginScreen').classList.add('d-none');
            document.getElementById('loginScreen').classList.remove('d-flex');
            document.getElementById('dashboardScreen').classList.remove('d-none');
            
            showToast('로그인에 성공했습니다!', true);
            fetchPortfolio();
            loadRanking('RISE');
        } else {
            showToast('로그인 실패! 아이디와 비밀번호를 확인하세요.', false);
        }
    } catch (error) {
        showToast('서버 통신 에러가 발생했습니다.', false);
    }
}

async function fetchPortfolio() {
    const response = await fetch('/api/portfolio/items', {
        method: 'GET',
        headers: { 'Authorization': 'Bearer ' + globalToken }
    });
    
    if (response.ok) {
        const data = await response.json();
        
        let exchangeRate = 1350; 
        try {
            const exRes = await fetch('https://open.er-api.com/v6/latest/USD');
            const exData = await exRes.json();
            exchangeRate = exData.rates.KRW;
        } catch(e) {
            console.log("환율 정보를 불러오지 못해 기본값을 사용합니다.");
        }

        const investedUsd = data.totalInvestedAmount;
        const totalUsd = data.totalPortfolioValue;
        const investedKrw = Math.floor(investedUsd * exchangeRate);
        const totalKrw = Math.floor(totalUsd * exchangeRate);
        
        document.getElementById('invested').innerHTML = `${investedUsd.toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2})} <small class="text-muted fs-6">USD</small>`;
        document.getElementById('investedKrw').innerText = `(약 ${investedKrw.toLocaleString()} 원)`;
        
        document.getElementById('totalValue').innerHTML = `${totalUsd.toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2})} <small class="text-muted fs-6">USD</small>`;
        document.getElementById('totalValueKrw').innerText = `(약 ${totalKrw.toLocaleString()} 원)`;
        
        const rateEl = document.getElementById('profitRate');
        rateEl.innerText = data.totalProfitRate.toFixed(2) + "%";
        rateEl.className = data.totalProfitRate >= 0 ? "text-danger fw-bold" : "text-primary fw-bold";

        const stockList = document.getElementById('stockList');
        stockList.innerHTML = "";
        
        data.items.forEach(item => {
            const isProfit = item.profitRate >= 0;
            const rateClass = isProfit ? "text-danger" : "text-primary";
            const icon = isProfit ? "🔺" : "🔹"; 
            
            const avgPrice = item.averagePrice.toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2});
            const curPrice = item.currentPrice.toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2});
            
            stockList.innerHTML += `
                <div class="col-md-6 mb-3">
                    <div class="card stock-card shadow-sm border-0">
                        <div class="card-body">
                            <div class="d-flex justify-content-between align-items-center mb-2">
                                <h5 class="card-title fw-bold m-0">${item.ticker}</h5>
                                <h4 class="${rateClass} m-0">${icon} ${item.profitRate.toFixed(2)}%</h4>
                            </div>
                            <p class="text-muted mb-1 text-truncate">${item.companyName || '종목명 정보 없음'}</p>
                            <hr>
                            <div class="d-flex justify-content-between align-items-center">
                                <span>보유 수량: <strong>${item.quantity}주</strong></span>
                                <span>평단가: <strong>$${avgPrice}</strong></span>
                            </div>
                            <div class="d-flex justify-content-between align-items-center mt-3">
                                <div>
                                    <button class="btn btn-sm btn-outline-dark me-1" onclick="openStockModal('${item.ticker}', ${item.currentPrice})">📊 상세 차트</button>
                                    <button class="btn btn-sm btn-outline-success me-1" onclick="prepareBuy('${item.ticker}', ${item.currentPrice})">추가 매수</button>
                                    <button class="btn btn-sm btn-outline-primary" onclick="sellStock('${item.ticker}', ${item.quantity})">전량 매도</button>
                                </div>
                                <span class="text-muted small">현재가: $${curPrice}</span>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        });
        
        drawChart(data.items);
    }
}

async function buyStock() {
    const ticker = document.getElementById('buyTicker').value.toUpperCase();
    const quantity = document.getElementById('buyQty').value;
    const price = document.getElementById('buyPrice').value;

    if(!ticker || !quantity || !price) {
        showToast("티커, 수량, 단가를 모두 입력해주세요.", false);
        return;
    }

    const response = await fetch('/api/portfolio/items', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + globalToken
        },
        body: JSON.stringify({ ticker: ticker, quantity: Number(quantity), averagePrice: Number(price) })
    });

    if (response.ok) {
        showToast(`[${ticker}] ${quantity}주 매수 체결 완료!`, true);
        document.getElementById('buyTicker').value = "";
        document.getElementById('buyQty').value = "";
        document.getElementById('buyPrice').value = "";
        fetchPortfolio(); 
    } else {
        showToast("매수 주문에 실패했습니다.", false);
    }
}

async function sellStock(ticker, quantity) {
    if(!confirm(`[${ticker}] ${quantity}주를 정말 매도하시겠습니까?`)) return;

    const response = await fetch('/api/portfolio/items/sell', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + globalToken
        },
        body: JSON.stringify({ ticker: ticker, quantity: quantity })
    });

    if (response.ok) {
        showToast(`[${ticker}] ${quantity}주 매도 체결 완료!`, true);
        fetchPortfolio(); 
    } else {
        showToast("매도 주문에 실패했습니다.", false);
    }
}

function prepareBuy(ticker, currentPrice) {
    document.getElementById('buyTicker').value = ticker;
    document.getElementById('buyPrice').value = currentPrice;
    
    const qtyInput = document.getElementById('buyQty');
    qtyInput.value = "";
    qtyInput.focus();

    window.scrollTo({ top: 0, behavior: 'smooth' });
}

function drawChart(items) {
    const ctx = document.getElementById('portfolioChart').getContext('2d');
    
    if (myChart !== null) {
        myChart.destroy();
    }

    if (!items || items.length === 0) {
        return; 
    }

    const labels = items.map(item => item.ticker);
    const dataValues = items.map(item => item.totalValue);
    const bgColors = ['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF', '#FF9F40', '#C9CBCF'];

    myChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [{
                data: dataValues,
                backgroundColor: bgColors.slice(0, labels.length), 
                borderWidth: 0, 
                hoverOffset: 4  
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom', 
                    labels: { boxWidth: 12 }
                }
            }
        }
    });
} 

function openStockModal(ticker, currentPrice) {
    document.getElementById('stockDetailModal').style.display = 'block';
    document.getElementById('modalTickerTitle').innerText = ticker;
    
    document.getElementById('modalCurrentPrice').value = currentPrice;
    document.getElementById('tradeQuantity').value = ''; 

    if (tvWidget !== null) {
        document.getElementById('tradingview_chart').innerHTML = '';
    }

    tvWidget = new TradingView.widget({
        "autosize": true,
        "symbol": ticker,
        "interval": "D",
        "timezone": "Asia/Seoul",
        "theme": "light",
        "style": "1",
        "locale": "kr",
        "enable_publishing": false,
        "hide_top_toolbar": false,
        "hide_legend": false,
        "save_image": false,
        "container_id": "tradingview_chart"
    });

    loadTradeHistory();
}

async function executeModalTrade(tradeType) {
    const ticker = document.getElementById('modalTickerTitle').innerText;
    
    const price = parseFloat(document.getElementById('modalCurrentPrice').value);
    const quantity = parseInt(document.getElementById('tradeQuantity').value);

    if (!quantity || quantity <= 0 || isNaN(price)) {
        alert("정확한 수량과 매매 단가를 입력해주세요.");
        return;
    }

    let url = tradeType === 'BUY' ? '/api/portfolio/items' : '/api/portfolio/items/sell';
    let bodyData = tradeType === 'BUY' 
        ? { ticker: ticker, quantity: quantity, averagePrice: price } 
        : { ticker: ticker, quantity: quantity };

    try {
        const response = await fetch(url, {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + globalToken 
            },
            body: JSON.stringify(bodyData)
        });

        if (response.ok) {
            const msg = await response.text();
            alert(`[${tradeType === 'BUY' ? '매수' : '매도'} 체결] ${msg}`);
            
            loadTradeHistory(); 
            fetchPortfolio();   
        } else {
            const err = await response.text();
            alert(`주문 실패: ${err}`);
        }
    } catch (error) {
        console.error("주문 중 에러 발생:", error);
    }
}

async function loadTradeHistory() {
    try {
        const response = await fetch('/api/history', {
            headers: { 'Authorization': 'Bearer ' + globalToken } 
        });
        if (!response.ok) throw new Error("내역을 불러오지 못했습니다.");
        
        const historyList = await response.json();
        const ul = document.getElementById('historyList');
        ul.innerHTML = ''; 

        historyList.forEach(record => {
            const li = document.createElement('li');
            li.style.padding = '8px';
            li.style.borderBottom = '1px solid #ddd';
            li.style.display = 'flex';
            li.style.justifyContent = 'space-between';
            li.style.alignItems = 'center';
            
            const dateStr = new Date(record.tradeDate).toLocaleString('ko-KR', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });

            li.innerHTML = `
                <span>
                    <b style="color: ${record.tradeType === 'BUY' ? 'red' : 'blue'};">
                        ${record.tradeType === 'BUY' ? '매수' : '매도'}
                    </b> 
                    <strong>${record.ticker}</strong> ${record.quantity}주 @ $${record.price}
                </span>
                <span style="font-size: 0.8em; color: gray;">
                    ${dateStr}
                    <button onclick="deleteHistory('${record.id}')" style="border:none; background:none; cursor:pointer; font-size: 1.2em;" title="기록 삭제">🗑️</button>
                </span>
            `;
            ul.appendChild(li);
        });
    } catch (error) {
        console.error(error);
    }
}

async function deleteHistory(id) {
    if (!confirm('이 거래 기록을 삭제하시겠습니까?')) return;
    try {
        const response = await fetch(`/api/history/${id}`, { 
            method: 'DELETE',
            headers: { 'Authorization': 'Bearer ' + globalToken } 
        });
        if (response.ok) {
            loadTradeHistory(); 
        } else {
            alert('삭제에 실패했습니다.');
        }
    } catch (error) {
        console.error(error);
    }
}

async function clearAllHistory() {
    if (!confirm('모든 거래 기록을 초기화하시겠습니까? (포트폴리오 주식은 그대로 유지됩니다)')) return;
    try {
        const response = await fetch(`/api/history/all`, { 
            method: 'DELETE',
            headers: { 'Authorization': 'Bearer ' + globalToken } 
        });
        if (response.ok) {
            loadTradeHistory();
            alert("거래 내역이 모두 초기화되었습니다.");
        }
    } catch (error) {
        console.error(error);
    }
}

async function searchStock() {
    const ticker = document.getElementById('searchInput').value.trim().toUpperCase();
    if (!ticker) {
        showToast("검색할 티커를 입력해주세요.", false);
        return;
    }
    
    showToast(`[${ticker}] 실시간 시세 조회 중...`, true);

    try {
        const response = await fetch(`/api/stocks/${encodeURIComponent(ticker)}`, {
            method: 'GET',
            headers: { 'Authorization': 'Bearer ' + globalToken }
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.detail || data.message || "시세를 가져오지 못했습니다.");
        }

        const currentPrice = Number(data.price);

        if (!Number.isFinite(currentPrice) || currentPrice <= 0) {
            throw new Error("현재가가 유효하지 않습니다.");
        }

        openStockModal(ticker, currentPrice);
        document.getElementById('searchInput').value = '';

    } catch (error) {
        console.error("검색 에러:", error);
        showToast(error.message || "종목 시세 조회에 실패했습니다.", false);
    }
}

async function loadRanking(type) {
    document.getElementById('btnRise').classList.toggle('active', type === 'RISE');
    document.getElementById('btnFall').classList.toggle('active', type === 'FALL');
    document.getElementById('btnActive').classList.toggle('active', type === 'ACTIVE');

    const rankingList = document.getElementById('rankingList');
    if (!rankingList) return;
    
    rankingList.innerHTML = '<p class="text-muted text-center mt-4">데이터 로딩 중...</p>';

    try {
        let apiUrl = '/api/ranking/rise';
        if (type === 'FALL') apiUrl = '/api/ranking/fall';
        if (type === 'ACTIVE') apiUrl = '/api/ranking/active';
        
        const response = await fetch(apiUrl, {
            method: 'GET',
            headers: { 
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + globalToken 
            }
        });

        if (!response.ok) throw new Error("랭킹 데이터를 불러오지 못했습니다.");

        const data = await response.json(); 

        if (!Array.isArray(data) || data.length === 0) {
            rankingList.innerHTML = '<p class="text-muted text-center mt-4">랭킹 데이터가 없습니다.</p>';
            return;
        }

        rankingList.innerHTML = '';
        
        data.forEach((item, index) => {
            const isProfit = parseFloat(item.rate) >= 0;
            const rateColor = isProfit ? 'text-danger' : 'text-primary';
            const sign = isProfit ? '+' : '';

            rankingList.innerHTML += `
                <div class="d-flex justify-content-between align-items-center p-2 mb-1 border-bottom" 
                     style="cursor: pointer; border-radius: 5px;" 
                     onmouseover="this.style.backgroundColor='#f8f9fa'" 
                     onmouseout="this.style.backgroundColor='transparent'"
                     onclick="openStockModal('${item.ticker}', ${item.price})">
                    <div>
                        <span class="fw-bold text-secondary me-2">${index + 1}</span>
                        <strong class="me-1">${item.ticker}</strong>
                        <span class="text-muted small text-truncate" style="display:inline-block; max-width:80px; vertical-align:bottom;">${item.name || ''}</span>
                    </div>
                    <div>
                        <span class="fw-bold me-2">$${parseFloat(item.price).toFixed(2)}</span>
                        <span class="${rateColor} fw-bold">${sign}${item.rate}%</span>
                    </div>
                </div>
            `;
        });
    } catch (error) {
        console.error("랭킹 로드 에러:", error);
        rankingList.innerHTML = '<p class="text-danger text-center mt-4">랭킹을 불러오는데 실패했습니다.</p>';
    }
}

let isIdChecked = false;

function openSignupModal() {
    document.getElementById('signupId').value = '';
    document.getElementById('signupPw').value = '';
    document.getElementById('signupPwConfirm').value = '';
    document.getElementById('idCheckMsg').className = "form-text text-muted";
    document.getElementById('idCheckMsg').innerText = "아이디 중복확인을 해주세요.";
    document.getElementById('pwCheckMsg').innerText = '';
    isIdChecked = false;
    document.getElementById('btnSubmitSignup').disabled = true;

    const modal = new bootstrap.Modal(document.getElementById('signupModal'));
    modal.show();
}

function resetIdCheck() {
    isIdChecked = false;
    const msgEl = document.getElementById('idCheckMsg');
    msgEl.className = "form-text text-muted";
    msgEl.innerText = "아이디 중복확인을 해주세요.";
    checkFormValid();
}

async function checkDuplicateId() {
    const loginId = document.getElementById('signupId').value.trim();
    const msgEl = document.getElementById('idCheckMsg');

    if (!loginId) {
        msgEl.className = "form-text text-danger";
        msgEl.innerText = "아이디를 입력해주세요.";
        return;
    }

    try {
        const response = await fetch(`/api/auth/check-id?loginId=${encodeURIComponent(loginId)}`);
        
        if (!response.ok) {
            msgEl.className = "form-text text-danger";
            msgEl.innerText = "서버 접근 권한 오류가 발생했습니다.";
            isIdChecked = false;
            return;
        }

        const isDuplicate = await response.json();

        if (isDuplicate === true || isDuplicate === "true") {
            msgEl.className = "form-text text-danger";
            msgEl.innerText = "이미 사용 중인 아이디입니다.";
            isIdChecked = false;
        } else {
            msgEl.className = "form-text text-success";
            msgEl.innerText = "사용 가능한 아이디입니다!";
            isIdChecked = true;
        }
        checkFormValid();
    } catch (e) {
        console.error(e);
        showToast("중복확인 통신 실패", false);
    }
}

function validatePassword() {
    const pw = document.getElementById('signupPw').value;
    const pwConfirm = document.getElementById('signupPwConfirm').value;
    const msgEl = document.getElementById('pwCheckMsg');

    if (!pwConfirm) {
        msgEl.innerText = '';
        checkFormValid();
        return;
    }

    if (pw === pwConfirm) {
        msgEl.className = "form-text text-success";
        msgEl.innerText = "비밀번호가 일치합니다.";
    } else {
        msgEl.className = "form-text text-danger";
        msgEl.innerText = "비밀번호가 일치하지 않습니다.";
    }
    checkFormValid();
}

function checkFormValid() {
    const pw = document.getElementById('signupPw').value;
    const pwConfirm = document.getElementById('signupPwConfirm').value;
    const isPwValid = pw && pwConfirm && (pw === pwConfirm);

    document.getElementById('btnSubmitSignup').disabled = !(isIdChecked && isPwValid);
}

async function completeSignup() {
    const loginId = document.getElementById('signupId').value.trim();
    const password = document.getElementById('signupPw').value;

    try {
        const response = await fetch('/api/auth/signup', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ 
                loginId: loginId, 
                password: password,
                name: loginId 
            })
        });

        if (response.ok) {
            const modalEl = document.getElementById('signupModal');
            const modal = bootstrap.Modal.getInstance(modalEl);
            modal.hide();

            showToast("회원가입 완료! 로그인해 주세요.", true);
            document.getElementById('loginId').value = loginId;
            document.getElementById('password').value = password;
        } else {
            const errorMsg = await response.text();
            showToast(`회원가입 실패: ${errorMsg}`, false);
        }
    } catch (error) {
        showToast('서버 통신 에러가 발생했습니다.', false);
    }
}

function logout() {
    globalToken = "";
    showToast('안전하게 로그아웃 되었습니다.', true);
    
    setTimeout(() => {
        window.location.reload();
    }, 1200);
}

async function withdrawAccount() {
    if (!confirm('정말로 회원 탈퇴를 진행하시겠습니까?\n모든 데이터가 영구적으로 삭제됩니다.')) {
        return;
    }

    try {
        const response = await fetch('/api/auth/withdraw', {
            method: 'DELETE',
            headers: {
                'Authorization': 'Bearer ' + globalToken
            }
        });

        if (response.ok) {
            alert('회원 탈퇴가 완료되었습니다. 이용해 주셔서 감사합니다.');
            window.location.reload();
        } else {
            const errorMsg = await response.text();
            showToast(`회원 탈퇴 실패: ${errorMsg}`, false);
        }
    } catch (error) {
        console.error(error);
        showToast('서버 통신 중 에러가 발생했습니다.', false);
    }
}
