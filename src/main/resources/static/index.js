// Busca um operador aleatório da API
async function fetchRandomOperator() {
    try {
        const response = await fetch('http://localhost:8080/api/random-operator');

        if (!response.ok) {
            throw new Error(`Erro ao buscar dados: ${response.status}`);
        }

        const jsonData = await response.json(); // Resposta JSON direta
        console.log("JSON retornado pela API:", jsonData);

        // Salva no cache
        localStorage.setItem('randomOperator', JSON.stringify(jsonData));
    } catch (error) {
        console.error('Erro ao buscar ou processar dados da API:', error);
    }
}

// Busca todos os nomes de operadores
async function fetchOperatorNames() {
    try {
        const response = await fetch('http://localhost:8080/api/all-operator-name');
        if (!response.ok) throw new Error(`Erro: ${response.status}`);

        const data = await response.json();
        initializeFilter(data);
    } catch (error) {
        console.error('Erro ao buscar dados da API:', error);
    }
}

// Inicializa o filtro de operadores
function initializeFilter(operators) {
    const input = document.getElementById('filter-input');
    const list = document.getElementById('filtered-list');

    const updateList = (data) => {
        const sortedData = data.sort((a, b) => a.name.localeCompare(b.name));

        list.innerHTML = sortedData
            .map(op => `
                <li onclick="setInputText('${op.name}')">
                    <div class="operator-icon" style="background-image: url('${op.icon || 'default-image.png'}');"></div>
                    <span class="operator-name">${op.name}</span>
                </li>`).join('');
    };

    input.addEventListener('input', () => {
        const filterText = input.value.toLowerCase();
        const filteredOperators = operators.filter(op => op.name.toLowerCase().includes(filterText));
        updateList(filteredOperators);
    });

    updateList(operators);
}

// Valida todos os campos do operador escolhido contra o operador aleatório
async function validateOperatorFields() {
    try {
        const randomOperator = JSON.parse(localStorage.getItem('randomOperator'));
        const chosenOperatorName = document.getElementById('filter-input').value.trim();

        if (!randomOperator || !chosenOperatorName) {
            alert("Both a random operator and your choice are required.");
            return;
        }

        const response = await fetch(`http://localhost:8080/api/validate-all-fields?random=${encodeURIComponent(randomOperator.name)}&choose=${encodeURIComponent(chosenOperatorName)}`);
        if (!response.ok) throw new Error(`Failed to validate operator: ${response.status}`);

        const { random, choose, validation } = await response.json();

        displayValidationResults(choose, validation);

    } catch (error) {
        console.error('Error validating operator fields:', error);
        alert("An error occurred while validating the operator. Please try again.");
    }
}

// Exibe os resultados da validação no HTML
function displayValidationResults(operator, validation) {
    const operatorTable = document.getElementById('operator-table');
    operatorTable.innerHTML = ''; // Limpa a tabela antes de exibir os resultados

    Object.entries(operator).forEach(([key, value]) => {
        const isCorrect = validation[key] ? 'correct' : 'wrong';
        operatorTable.innerHTML += `
            <div class="cell ${isCorrect}">
                <strong>${key}:</strong> ${value || 'Unknown'}
            </div>
        `;
    });
}

// Define o texto de entrada do filtro
function setInputText(nameOperator) {
    document.getElementById('filter-input').value = nameOperator;
}

// Inicializa os operadores ao carregar a página
function initializeOperator() {
    const cachedOperator = localStorage.getItem('randomOperator');

    fetchOperatorNames();

    if (cachedOperator) {
        const operatorData = JSON.parse(cachedOperator);
        console.log("Carregando dados do cache:", operatorData);
    } else {
        fetchRandomOperator();
    }
}

async function searchOperator() {
    try {
        // Obtém o nome do operador aleatório salvo no localStorage
        const randomOperator = localStorage.getItem('randomOperator');
        console.log("Raw data from localStorage (randomOperator):", randomOperator);

        if (!randomOperator) {
            alert("No random operator found in localStorage.");
            return;
        }

        const parsedRandomOperator = JSON.parse(randomOperator);
        console.log("Parsed randomOperator data:", parsedRandomOperator);

        const random = parsedRandomOperator.name; // Extrai o nome do operador aleatório
        console.log("Random operator name extracted:", random);

        // Obtém o valor digitado no input como o operador escolhido
        const chosen = document.getElementById('filter-input').value.trim();
        console.log("Chosen operator name from input:", chosen);

        if (!chosen) {
            alert("Please enter a valid operator name.");
            return;
        }

        // Faz a requisição para validar os campos
        console.log("Making API call to validate fields...");
        const response = await fetch(`http://localhost:8080/api/validate-all-fields?random=${encodeURIComponent(random)}&choose=${encodeURIComponent(chosen)}`);
        console.log("API Response status:", response.status);

        if (!response.ok) {
            throw new Error(`Failed to validate fields: ${response.status}`);
        }

        // Processa a resposta da API
        const validationResult = await response.json();
        console.log("Validation Results JSON:", validationResult);

        // Exibe o status de validação no console
        for (const [field, status] of Object.entries(validationResult.status)) {
            console.log(`Field: ${field}, Status: ${status}`);
        }
    } catch (error) {
        console.error('Error validating fields:', error);
    }
}

document.addEventListener('DOMContentLoaded', initializeOperator);
