let playerId = null;
let currentSeq = 0;
let myChoice = null;
let opponentAnswered = false;
let myReady = false;
let opponentReady = false;
let sseSource = null;

function showScreen(id) {
    ['waitingScreen', 'readyScreen', 'quizScreen', 'resultScreen', 'errorScreen'].forEach(s => {
        document.getElementById(s).classList.add('hidden');
    });
    const el = document.getElementById(id);
    el.classList.remove('hidden');
    el.classList.add('fade-in');
    setTimeout(() => el.classList.remove('fade-in'), 400);
}

function showError(msg) {
    document.getElementById('errorText').textContent = msg;
    showScreen('errorScreen');
}

// ---- WAITING SCREEN ----
function initWaitingScreen() {
    showScreen('waitingScreen');
    document.getElementById('roomCodeDisplay').textContent = ROOM_CODE;
    new QRCode(document.getElementById('qrcode'), {
        text: ROOM_URL,
        width: 180,
        height: 180,
        colorDark: '#1a1a1a',
        colorLight: '#ffffff',
        correctLevel: QRCode.CorrectLevel.M
    });
}

// ---- READY SCREEN ----
function initReadyScreen(status) {
    showScreen('readyScreen');
    updateReadyIcons(status.myReady, status.readyCount - (status.myReady ? 1 : 0) > 0);
}

function updateReadyIcons(iAmReady, opponentIsReady) {
    myReady = iAmReady;
    opponentReady = opponentIsReady;
    if (iAmReady) {
        document.getElementById('readyBefore').classList.add('hidden');
        document.getElementById('readyWaiting').classList.remove('hidden');
    }
}

async function pressStart() {
    const btn = document.getElementById('startBtn');
    btn.disabled = true;
    btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin mr-2"></i>준비 중...';
    try {
        await fetch(CONTEXT_PATH + `/api/rooms/${ROOM_CODE}/ready`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ playerId })
        });
        updateReadyIcons(true, opponentReady);
    } catch (e) {
        btn.disabled = false;
        btn.innerHTML = '<i class="fa-solid fa-play mr-2"></i>시작하기';
    }
}

// ---- QUIZ SCREEN ----
function updateQuestion(seq, optionA, optionB) {
    currentSeq = seq;
    myChoice = null;
    opponentAnswered = false;

    document.getElementById('seqLabel').textContent = `${seq} / 10`;
    document.getElementById('progressBar').style.width = `${seq * 10}%`;
    document.getElementById('optionAText').textContent = optionA;
    document.getElementById('optionBText').textContent = optionB;

    const btnA = document.getElementById('optionA');
    const btnB = document.getElementById('optionB');
    btnA.className = 'btn-option flex-1 bg-white rounded-2xl p-4 text-center';
    btnB.className = 'btn-option flex-1 bg-white rounded-2xl p-4 text-center';
    btnA.disabled = false;
    btnB.disabled = false;
    document.getElementById('waitingOpponent').classList.add('hidden');
}

function showQuestion(seq, optionA, optionB) {
    updateQuestion(seq, optionA, optionB);
    showScreen('quizScreen');
}

async function submitAnswer(choice) {
    myChoice = choice;
    const btnA = document.getElementById('optionA');
    const btnB = document.getElementById('optionB');
    btnA.disabled = true;
    btnB.disabled = true;

    if (choice === 'A') {
        btnA.classList.add('selected-A');
    } else {
        btnB.classList.add('selected-B');
    }

    document.getElementById('waitingOpponent').classList.remove('hidden');

    try {
        await fetch(CONTEXT_PATH + `/api/rooms/${ROOM_CODE}/answers`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ playerId, seq: currentSeq, choice })
        });
    } catch (e) {
        console.error('Answer submit failed', e);
    }
}

// ---- RESULT SCREEN ----
async function showResults() {
    try {
        const res = await fetch(CONTEXT_PATH + `/api/rooms/${ROOM_CODE}/results?playerId=${encodeURIComponent(playerId)}`);
        const data = await res.json();
        renderResults(data);
    } catch (e) {
        console.error('Results fetch failed', e);
    }
}

function renderResults(data) {
    const rate = data.matchRate;
    document.getElementById('matchRateText').textContent = `${rate}%`;
    setTimeout(() => {
        document.getElementById('resultBar').style.width = `${rate}%`;
    }, 100);

    let emoji = '💔', comment = '우리 아직 서로를 더 알아가야 해요!';
    if (rate >= 90) { emoji = '💞'; comment = '완벽한 공감! 천생연분이에요 💕'; }
    else if (rate >= 70) { emoji = '💕'; comment = '꽤 잘 맞는군요! 좋은 케미예요 ✨'; }
    else if (rate >= 50) { emoji = '💛'; comment = '절반은 통했네요. 더 알아가봐요 🌸'; }
    else if (rate >= 30) { emoji = '💙'; comment = '서로 다른 매력이 있네요 🌈'; }

    document.getElementById('resultEmoji').textContent = emoji;
    document.getElementById('matchComment').textContent = comment;

    const list = document.getElementById('answerList');
    list.innerHTML = data.questions.map(q => `
        <div class="flex items-center gap-2 p-2 rounded-xl ${q.matched ? 'bg-pink-50' : 'bg-gray-50'}">
            <span class="text-lg">${q.matched ? '💕' : '💔'}</span>
            <div class="flex-1 min-w-0">
                <div class="flex gap-1 text-xs">
                    <span class="bg-pink-200 text-pink-700 px-2 py-0.5 rounded-full truncate">나: ${q.myChoice === 'A' ? q.optionA : q.optionB}</span>
                    <span class="bg-purple-200 text-purple-700 px-2 py-0.5 rounded-full truncate">상대: ${q.opponentChoice === 'A' ? q.optionA : q.optionB}</span>
                </div>
            </div>
        </div>
    `).join('');

    showScreen('resultScreen');
}

function copyLink() {
    const text = `공감퀴즈 결과 공유!\n우리의 공감률은 ${document.getElementById('matchRateText').textContent} 💕\n${ROOM_URL}`;
    navigator.clipboard.writeText(text).then(() => alert('링크가 복사됐어요! 📋'));
}

function shareKakao() {
    if (window.Kakao && Kakao.isInitialized()) {
        Kakao.Share.sendDefault({
            objectType: 'feed',
            content: {
                title: '공감퀴즈 결과 💕',
                description: `우리의 공감률은 ${document.getElementById('matchRateText').textContent}! 나도 테스트해봐!`,
                imageUrl: `${BASE_URL}/img/og-image.png`,
                link: { mobileWebUrl: ROOM_URL, webUrl: ROOM_URL },
            },
            buttons: [{ title: '결과 보기', link: { mobileWebUrl: ROOM_URL, webUrl: ROOM_URL } }],
        });
    } else {
        const url = `https://sharer.kakao.com/talk/friends/picker/link?app_key=&url=${encodeURIComponent(ROOM_URL)}`;
        window.open(url, '_blank', 'width=400,height=600');
    }
}

function shareFacebook() {
    window.open(`https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(ROOM_URL)}`, '_blank', 'width=600,height=400');
}

function shareInstagram() {
    navigator.clipboard.writeText(ROOM_URL).then(() => {
        alert('링크가 복사됐어요! 인스타그램 앱에서 붙여넣기 해주세요 📋');
    });
}

function shareThreads() {
    const rate = document.getElementById('matchRateText').textContent;
    const text = encodeURIComponent(`공감퀴즈 결과 💕 우리의 공감률은 ${rate}! #공감퀴즈`);
    window.open(`https://www.threads.net/intent/post?text=${text}%20${encodeURIComponent(ROOM_URL)}`, '_blank');
}

function shareTwitter() {
    const rate = document.getElementById('matchRateText').textContent;
    const text = encodeURIComponent(`공감퀴즈 결과 💕 우리의 공감률은 ${rate}! #공감퀴즈`);
    window.open(`https://twitter.com/intent/tweet?text=${text}&url=${encodeURIComponent(ROOM_URL)}`, '_blank');
}

function playAgain() {
    localStorage.removeItem(PLAYER_KEY);
    window.location.href = CONTEXT_PATH + '/';
}

// ---- SSE ----
function connectSse() {
    if (sseSource) sseSource.close();
    sseSource = new EventSource(CONTEXT_PATH + `/sse/rooms/${ROOM_CODE}`);

    sseSource.addEventListener('PLAYER_JOINED', () => {
        initReadyScreen({ myReady: false, readyCount: 0 });
    });

    sseSource.addEventListener('PLAYER_READY', (e) => {
        const d = JSON.parse(e.data);
        const iAmReady = myReady;
        const opponentNowReady = d.data.readyCount - (iAmReady ? 1 : 0) > 0;
        updateReadyIcons(iAmReady, opponentNowReady);
    });

    sseSource.addEventListener('QUIZ_STARTED', (e) => {
        const d = JSON.parse(e.data);
        showQuestion(d.data.seq, d.data.optionA, d.data.optionB);
    });

    sseSource.addEventListener('OPPONENT_ANSWERED', () => {
        opponentAnswered = true;
    });

    sseSource.addEventListener('QUESTION_ADVANCED', (e) => {
        const d = JSON.parse(e.data);
        setTimeout(() => updateQuestion(d.data.seq, d.data.optionA, d.data.optionB), 800);
    });

    sseSource.addEventListener('QUIZ_COMPLETED', () => {
        setTimeout(() => showResults(), 800);
    });

    sseSource.onerror = () => {
        console.warn('SSE disconnected, retrying...');
        setTimeout(connectSse, 3000);
    };
}

// ---- INIT ----
async function init() {
    playerId = localStorage.getItem(PLAYER_KEY);

    if (!playerId) {
        try {
            const res = await fetch(CONTEXT_PATH + `/api/rooms/${ROOM_CODE}/join`, { method: 'POST' });
            if (res.status === 409) {
                showError('이미 두 명이 참여 중인 방이에요');
                return;
            }
            if (!res.ok) {
                showError('방을 찾을 수 없어요');
                return;
            }
            const data = await res.json();
            playerId = data.playerId;
            localStorage.setItem(PLAYER_KEY, playerId);
        } catch (e) {
            showError('연결에 실패했어요. 다시 시도해주세요.');
            return;
        }
    }

    connectSse();

    try {
        const res = await fetch(CONTEXT_PATH + `/api/rooms/${ROOM_CODE}/status?playerId=${encodeURIComponent(playerId)}`);
        if (!res.ok) {
            showError('방 정보를 불러올 수 없어요');
            return;
        }
        const status = await res.json();

        if (status.status === 'WAITING') {
            initWaitingScreen();
        } else if (status.status === 'JOINED') {
            initReadyScreen(status);
        } else if (status.status === 'IN_PROGRESS') {
            if (status.currentQuestion) {
                showQuestion(status.currentSeq, status.currentQuestion.optionA, status.currentQuestion.optionB);
                if (status.myAnsweredCurrentSeq) {
                    document.getElementById('optionA').disabled = true;
                    document.getElementById('optionB').disabled = true;
                    document.getElementById('waitingOpponent').classList.remove('hidden');
                }
            }
        } else if (status.status === 'COMPLETED') {
            await showResults();
        }
    } catch (e) {
        showError('연결에 실패했어요. 다시 시도해주세요.');
    }
}

init();
