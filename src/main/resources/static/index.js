let attemptsLeft = 7; // Número de tentativas restantes

// Obtém um operador aleatório da API
async function fetchRandomOperator() {
    try {
        const response = await fetch('http://localhost:8080/api/random-operator');
        if (!response.ok) throw new Error(`Erro ao buscar operador aleatório: ${response.status}`);

        const operator = await response.json();
        localStorage.setItem('randomOperator', JSON.stringify(operator));
    } catch (error) {
        console.error('Erro ao buscar operador aleatório:', error);
    }
}

// Busca todos os nomes de operadores e inicializa o filtro
async function fetchOperatorNames() {
    try {
        const response = await fetch('http://localhost:8080/api/all-operator-name');
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

    const updateList = (data) => {
        list.innerHTML = data
            .sort((a, b) => a.name.localeCompare(b.name))
            .map(op => `
                <li onclick="setInputText('${op.name}')">
                    <div class="operator-icon" style="background-image: url('${op.icon || 'default-image.png'}');"></div>
                    <span class="operator-name">${op.name}</span>
                </li>`).join('');
    };

    input.addEventListener('input', () => {
        const filter = input.value.toLowerCase();
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

        const response = await fetch(`http://localhost:8080/api/validate-all-fields?random=${encodeURIComponent(randomOperator)}&choose=${encodeURIComponent(chosenOperatorName)}`);
        if (!response.ok) throw new Error(`Erro ao validar operadores: ${response.status}`);

        const responseText = await response.text();
        document.getElementById('operator-table').innerHTML += responseText;

        // Compara a resposta do usuário com o operador correto
        if (chosenOperatorName === randomOperator) {
            showModal(true); // Chama o modal de sucesso
        } else {
            attemptsLeft--;
            updateAttemptsCounter(); // Atualiza o contador de tentativas
            if (attemptsLeft == 0) {
                showModal(false, true, randomOperator); // Mostra o operador correto após 7 tentativas erradas
            }
        }

        // Limpa o campo de entrada
        document.getElementById('filter-input').value = "";

        // Recarrega todos os operadores na lista após a validação
        fetchOperatorNames();
    } catch (error) {
        console.error('Erro ao validar operadores:', error);
//        alert("Ocorreu um erro. Tente novamente.");
    }
}

// Atualiza o contador de tentativas
function updateAttemptsCounter() {
    const attemptsCounter = document.getElementById('attempts-counter');
    attemptsCounter.textContent = `Tentativas restantes: ${attemptsLeft}`;
}

// Obtém o nome do operador desencriptado
async function fetchDecryptedOperator(encryptedName) {
    try {
        const response = await fetch(`http://localhost:8080/api/decrypt-operator?name=${encodeURIComponent(encryptedName)}`);
        if (!response.ok) throw new Error(`Erro ao desencriptar nome: ${response.status}`);

        const result = await response.json();
        return result.decryptedName;
    } catch (error) {
        console.error('Erro ao desencriptar operador:', error);
        return null;
    }
}

// Exibe o modal com o nome desencriptado
async function showModal(isCorrect, gameOver = false, encryptedAnswer = "") {
    const modal = document.getElementById('success-modal');
    const modalTitle = document.getElementById('modal-title');
    const modalMessage = document.getElementById('modal-message');

    if (isCorrect) {
        modalTitle.textContent = "Operador Correto!";
        modalMessage.textContent = "Você escolheu o operador correto. Parabéns!";
    } else {
        if (gameOver && encryptedAnswer) {
            const decryptedName = await fetchDecryptedOperator(encryptedAnswer);
            modalTitle.textContent = "Game Over!";
            modalMessage.textContent = `Você errou! O operador correto era: ${decryptedName || "desconhecido"}.`;
        }
    }

    modal.style.display = "flex"; // Exibe o modal
}


// Fecha o modal e reinicia o jogo
function closeModal() {
    const modal = document.getElementById('success-modal');
    modal.style.display = "none";

    fetch('http://localhost:8080/api/restart-game');

    localStorage.clear();

    // Reinicia as tentativas e a tabela
    attemptsLeft = 7;
    updateAttemptsCounter(); // Atualiza o contador de tentativas

    // Limpa a tabela de operadores escolhidos
    document.getElementById('operator-table').innerHTML = "";

    // Reinicia o jogo
    fetchRandomOperator();
    fetchOperatorNames();

    document.getElementById('filter-input').value = ""
}

// Define o texto no campo de entrada
function setInputText(name) {
    document.getElementById('filter-input').value = name;
}

// Inicializa a aplicação ao carregar a página
function initialize() {
    if (!localStorage.getItem('randomOperator')) fetchRandomOperator();
    fetchOperatorNames();

    // Adiciona evento de clique no botão de busca
    document.getElementById('filter-btn').addEventListener('click', validateOperatorFields);
    document.getElementById('filter-input').value = "";
}

document.addEventListener('DOMContentLoaded', initialize);
