window.addEventListener('pageshow', (e) => {
    if (e.persisted) resetButtons();
});

function resetButtons() {
    const startBtn = document.getElementById('startBtn');
    startBtn.disabled = false;
    startBtn.innerHTML = '<i class="fa-solid fa-plus mr-2"></i>새 방 만들기';

    const joinBtn = document.getElementById('joinBtn');
    joinBtn.disabled = false;
    joinBtn.innerHTML = '<i class="fa-solid fa-arrow-right"></i>';

    document.getElementById('errorMsg').classList.add('hidden');
}

function showError(msg) {
    const errorMsg = document.getElementById('errorMsg');
    errorMsg.textContent = msg;
    errorMsg.classList.remove('hidden');
}

async function createRoom() {
    const btn = document.getElementById('startBtn');
    btn.disabled = true;
    btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin mr-2"></i>만드는 중...';
    document.getElementById('errorMsg').classList.add('hidden');

    try {
        const res = await fetch(CONTEXT_PATH + '/api/rooms', { method: 'POST' });
        if (!res.ok) throw new Error();
        const data = await res.json();
        localStorage.setItem(`ogam_${data.roomCode}_playerId`, data.playerId);
        window.location.href = CONTEXT_PATH + `/room/${data.roomCode}`;
    } catch {
        resetButtons();
        showError('오류가 발생했어요. 다시 시도해주세요.');
    }
}

async function joinRoom() {
    const code = document.getElementById('codeInput').value.trim().toUpperCase();
    if (code.length !== 6) {
        showError('방 코드는 6자리예요');
        document.getElementById('codeInput').focus();
        return;
    }

    const joinBtn = document.getElementById('joinBtn');
    joinBtn.disabled = true;
    joinBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i>';
    document.getElementById('errorMsg').classList.add('hidden');

    try {
        const res = await fetch(CONTEXT_PATH + `/api/rooms/${code}/join`, { method: 'POST' });
        if (res.status === 404) { resetButtons(); showError('존재하지 않는 방 코드예요'); return; }
        if (res.status === 409) { resetButtons(); showError('이미 두 명이 참여 중인 방이에요'); return; }
        if (!res.ok) throw new Error();
        const data = await res.json();
        localStorage.setItem(`ogam_${data.roomCode}_playerId`, data.playerId);
        window.location.href = CONTEXT_PATH + `/room/${data.roomCode}`;
    } catch {
        resetButtons();
        showError('오류가 발생했어요. 다시 시도해주세요.');
    }
}
