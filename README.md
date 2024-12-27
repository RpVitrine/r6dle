# R6dle

Este é um projeto simples de quiz baseado no jogo [Rainbow Six Siege](https://www.ubisoft.com/en-gb/game/rainbow-six/siege), 
onde os jogadores tentam adivinhar o operador correto com base nas pistas fornecidas. O backend é construído usando Ktor, 
e o frontend é uma aplicação web que consome a API do backend.

## Funcionalidades

- **Seleção de Operadores**: O backend fornece uma lista de operadores aleatórios e permite validar a seleção do jogador.
- **CORS Configurado**: A API está configurada para aceitar requisições CORS de origens específicas (`localhost:8080` e `127.0.0.1:8080`).
- **Validação de Operadores**: Os jogadores podem escolher um operador e compará-lo com um operador aleatório para validar a resposta.
- **Reinício do Jogo**: Ao acertar ou quando acabar as chances de acerto o jogo é reiniciado.

## Tecnologias

- **Ktor**: Framework Kotlin para a construção do backend.
- **Kotlin**: Linguagem utilizada para desenvolvimento tanto do backend quanto da lógica do jogo.
- **Ktor CORS**: Configuração de CORS para permitir requisições de diferentes origens.
- **JSON**: Formato usado para comunicação entre o frontend e o backend.
- **HTML, CSS e JavaScript**: Usados no frontend para criar a interface do usuário.

## Como Executar

### Backend

1. Clone este repositório:
    ```
    git clone https://github.com/rpvitrine/r6dle.git
    cd r6dle
    ```

2. Abra o projeto no seu IDE.

3. Certifique-se de que as dependências do projeto estejam corretamente instaladas.

4. Compile e execute o projeto no Ktor. O servidor backend estará disponível em `http://localhost:8080` ou `127.0.0.1:8080`.

## Endpoints da API

### `GET /api/random-operator`
Retorna um operador aleatório da lista de operadores.

### `GET /api/all-operator-name`
Retorna os nomes e os icons dos operadores.

### `GET /api/restart-game`
Reinicia o jogo, limpando todas as variaveis.

### `GET /api/validate-all-fields`
Recebe o operador aleatorio e o que o utilizador escolheu, verifica e retorna o que está errado ou correto.
