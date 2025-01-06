(() => {
    let attemptsLeft = 7; // Número de tentativas restantes
    let streak = 0;

    const API_BASE_URL = 'http://localhost:8080/api';
    const DEFAULT_ICON = 'default-image.png';

    // Busca um operador aleatório e salva no localStorage
    async function fetchRandomOperator() {
        try {
            const response = await fetch(`${API_BASE_URL}/random-operator`);
            if (!response.ok) throw new Error(`Erro ao buscar operador aleatório: ${response.status}`);

            const operator = await response.json();
            localStorage.setItem('randomOperator', JSON.stringify(operator));
        } catch (error) {
            console.error('Erro ao buscar operador aleatório:', error);
        }
    }

    // Busca todos os nomes de operadores
    async function fetchOperatorNames() {
        try {
            const response = await fetch(`${API_BASE_URL}/all-operator-name`);
            if (!response.ok) throw new Error(`Erro ao buscar nomes de operadores: ${response.status}`);

            const operators = await response.json();
            setupFilter(operators);
        } catch (error) {
            console.error('Erro ao buscar nomes de operadores:', error);
        }
    }

    // Configura o filtro de operadores
    function setupFilter(operators) {
        const input = document.getElementById('filter-input');
        const list = document.getElementById('filtered-list');

        const updateList = (filteredOperators) => {
            list.innerHTML = filteredOperators
                .sort((a, b) => a.name.localeCompare(b.name))
                .map(op => `
                    <li onclick="setInputText('${op.name}')">
                        <div class="operator-icon" style="background-image: url('${op.icon || DEFAULT_ICON}');"></div>
                        <span class="operator-name">${op.name}</span>
                    </li>`).join('');
        };

        input.addEventListener('input', () => {
            const filter = input.value.trim().toLowerCase();
            updateList(operators.filter(op => op.name.toLowerCase().includes(filter)));
        });

        updateList(operators);
    }

    // Valida os operadores escolhidos
    async function validateOperatorFields() {
        try {
            const randomOperator = JSON.parse(localStorage.getItem('randomOperator'));
            const chosenOperatorName = document.getElementById('filter-input').value.trim();

            if (!randomOperator || !chosenOperatorName) {
                alert("Por favor, escolha um operador.");
                return;
            }

            const validateResponse = await fetch(`${API_BASE_URL}/validate-all-fields?random=${encodeURIComponent(randomOperator)}&choose=${encodeURIComponent(chosenOperatorName)}`);
            if (!validateResponse.ok) throw new Error(`Erro ao validar operadores: ${validateResponse.status}`);

            const responseText = await validateResponse.text();
            document.getElementById('operator-table').innerHTML += responseText;

            const isCorrect = await ValidateOperatorCorrect(chosenOperatorName.toLowerCase(), randomOperator);
            if (!isCorrect) return;

            if (isCorrect == "true") {
                showModal(true); // Chama o modal de sucesso
            } else {
                handleIncorrectAttempt(randomOperator);
            }

            document.getElementById('filter-input').value = ""; // Limpa o campo
            fetchOperatorNames(); // Atualiza a lista
        } catch (error) {
            console.error('Erro ao validar operadores:', error);
        }
    }

    // Busca o nome desencriptado do operador
    async function ValidateOperatorCorrect(chosseName, randomOperator) {
        try {
            const response = await
                fetch(`${API_BASE_URL}/validate-operator?chosse=${encodeURIComponent(chosseName)}&&random=${encodeURIComponent(randomOperator)}`);
            if (!response.ok) throw new Error(`Erro ao desencriptar operador: ${response.status}`);

            return (await response.text()).trim();
        } catch (error) {
            console.error('Erro ao desencriptar operador:', error);
            return null;
        }
    }

    function updateStreak() {
        const streakCounter = document.getElementById('streak-counter');
        streakCounter.textContent = `Streak: ${streak}`;
    }

    // Lida com tentativas incorretas
    function handleIncorrectAttempt(randomOperator) {
        attemptsLeft--;
        updateAttemptsCounter();

        if (attemptsLeft === 0) {
            showModal(false, true, randomOperator);
        }
    }

    // Atualiza o contador de tentativas
    function updateAttemptsCounter() {
        const attemptsCounter = document.getElementById('attempts-counter');
        attemptsCounter.textContent = `Tentativas restantes: ${attemptsLeft}`;
    }

    // Exibe o modal de resultado
    async function showModal(isCorrect, gameOver = false, encryptedAnswer = "") {
        const modal = document.getElementById('success-modal');
        const modalTitle = document.getElementById('modal-title');
        const modalMessage = document.getElementById('modal-message');

        if (isCorrect) {
            modalTitle.textContent = "Operador Correto!";
            modalMessage.textContent = "Você escolheu o operador correto. Parabéns!";
            streak++;
            updateStreak();
        } else if (gameOver && encryptedAnswer) {
            const decryptedName = await fetchDecryptedOperator(encryptedAnswer);
            modalTitle.textContent = "Game Over!";
            modalMessage.textContent = `Você errou! O operador correto era: ${decryptedName || "desconhecido"}.`;
            streak = 0;
            updateStreak();
        }

        modal.style.display = "flex"; // Exibe o modal
    }

    // Fecha o modal e reinicia o jogo
    function closeModal() {
        const modal = document.getElementById('success-modal');
        modal.style.display = "none";

        fetch(`${API_BASE_URL}/restart-game`)

        localStorage.clear();
        attemptsLeft = 7;
        updateAttemptsCounter();
        document.getElementById('operator-table').innerHTML = "";

        fetchRandomOperator();
        fetchOperatorNames();
    }

    // Define o texto no campo de entrada
    window.setInputText = function (name) {
        document.getElementById('filter-input').value = name;
    };

//    function hasPageRefreshed() {
//        return performance.navigation.type === 1; // 1 significa recarregamento
//    }

    // Inicializa a aplicação
    function initialize() {
        localStorage.clear();
        fetch(`${API_BASE_URL}/restart-game`)

//        if (hasPageRefreshed()) {
//            console.log("A página foi recarregada!");
//            localStorage.clear();
//            attemptsLeft = 7;
//            updateAttemptsCounter();
//            document.getElementById('operator-table').innerHTML = "";
//        }

        if (!localStorage.getItem('randomOperator'))
            {fetchRandomOperator();}
        fetchOperatorNames();

        updateStreak();

        document.getElementById('filter-btn').addEventListener('click', validateOperatorFields);
        document.getElementById('close-modal-btn').addEventListener('click', closeModal);
        updateAttemptsCounter();
    }

    document.addEventListener('DOMContentLoaded', initialize);
})();