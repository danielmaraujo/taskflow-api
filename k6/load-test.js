import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

export let errorRate = new Rate('errors');

// Configuração do teste
export let options = {
    stages: [
        { duration: '1m', target: 10 },    // Rampa de subida para 10 usuários em 1 minuto
        { duration: '3m', target: 10 },    // Mantém 10 usuários por 3 minutos
        { duration: '1m', target: 20 },    // Rampa de subida para 20 usuários em 1 minuto
        { duration: '4m', target: 20 },    // Mantém 20 usuários por 4 minutos
        { duration: '1m', target: 0 },     // Rampa de descida para 0 usuários em 1 minuto
    ],
    thresholds: {
        http_req_duration: ['p(95)<1500'], // 95% das requisições devem ser < 1500ms
        http_req_failed: ['rate<0.1'],     // Taxa de erro < 10%
        errors: ['rate<0.1'],
    },
};

// Configuração da API
const BASE_URL = __ENV.API_URL || 'https://taskflow-api-1014288660691.us-central1.run.app';
const API_ENDPOINTS = {
    auth: `${BASE_URL}/api/auth`,
    login: `${BASE_URL}/api/auth/login`,
    signup: `${BASE_URL}/api/auth/signup`,
    tasks: `${BASE_URL}/api/task`,
    allTasks: `${BASE_URL}/api/task/all`
};

// Gerador de dados aleatórios para usuários
function generateRandomUser() {
    const id = Math.floor(Math.random() * 100000);
    const username = `user_${id}_${Date.now().toString().slice(-5)}`;
    const email = `${username}@teste.com`;
    const password = `senha_${Math.random().toString(36).substring(2, 10)}`;

    return { username, email, password };
}

// Dados de teste - usuários gerados aleatoriamente
const testUsers = [
    generateRandomUser(),
    generateRandomUser(),
    generateRandomUser()
];

export function setup() {
    // Registrar usuários de teste
    testUsers.forEach(user => {
        const registerPayload = {
            name: user.username,
            password: user.password,
            email: user.email
        };

        http.post(`${API_ENDPOINTS.signup}`, JSON.stringify(registerPayload), {
            headers: { 'Content-Type': 'application/json' },
        });
    });

    // Fazer login e obter tokens
    const tokens = [];
    testUsers.forEach(user => {
        const loginPayload = {
            email: user.email,
            password: user.password
        };

        const response = http.post(`${API_ENDPOINTS.login}`, JSON.stringify(loginPayload), {
            headers: { 'Content-Type': 'application/json' },
        });

        if (response.status === 200) {
            const token = JSON.parse(response.body).token;
            tokens.push(token);
        }
    });

    return { tokens: tokens };
}

export default function(data) {
    // Selecionar token aleatório
    const token = data.tokens[Math.floor(Math.random() * data.tokens.length)];
    const headers = {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
    };

    // Cenário 1: Listar tasks (70% das requisições)
    if (Math.random() < 0.7) {
        const response = http.get(API_ENDPOINTS.allTasks, { headers });

        const success = check(response, {
            'GET /tasks status is 200': (r) => r.status === 200,
            'GET /tasks response time < 1500ms': (r) => r.timings.duration < 1500,
        });

        if (!success) {
            errorRate.add(1);
        }
    }

    // Cenário 2: Criar nova task (20% das requisições)
    else if (Math.random() < 0.9) {
        const taskPayload = {
            title: `Tarefa ${Math.random().toString(36).substring(7)}`,
            description: `Descrição da tarefa criada em ${new Date().toISOString()}`,
            limitDate: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0] // 7 dias a partir de agora
        };

        const response = http.post(API_ENDPOINTS.tasks, JSON.stringify(taskPayload), { headers });

        const success = check(response, {
            'POST /tasks status is 201': (r) => r.status === 201,
            'POST /tasks response time < 1500ms': (r) => r.timings.duration < 1500,
            'POST /tasks returns task ID': (r) => {
                try {
                    const body = JSON.parse(r.body);
                    return body.id !== undefined;
                } catch (e) {
                    return false;
                }
            }
        });

        if (!success) {
            errorRate.add(1);
        }
    }

    // Cenário 3: Atualizar task existente (10% das requisições)
    else {
        // Primeiro, obter uma task existente
        const listResponse = http.get(API_ENDPOINTS.allTasks, { headers });

        if (listResponse.status === 200) {
            try {
                const tasks = JSON.parse(listResponse.body);
                if (tasks.length > 0) {
                    const randomTask = tasks[Math.floor(Math.random() * tasks.length)];
                    const updatePayload = {
                        title: `Atualizado ${randomTask.title}`,
                        description: `Descrição atualizada em ${new Date().toISOString()}`,
                        limitDate: randomTask.limitDate || new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
                        status: randomTask.status || 'OPEN'
                    };

                    const response = http.put(`${API_ENDPOINTS.tasks}/${randomTask.id}`,
                        JSON.stringify(updatePayload), { headers });

                    const success = check(response, {
                        'PUT /tasks/{id} status is 200': (r) => r.status === 200,
                        'PUT /tasks/{id} response time < 1500ms': (r) => r.timings.duration < 1500,
                    });

                    if (!success) {
                        errorRate.add(1);
                    }
                } else {
                    errorRate.add(1);
                }
            } catch (e) {
                errorRate.add(1);
            }
        } else {
            errorRate.add(1);
        }
    }

    sleep(1); // Pausa de 1 segundo entre requisições
}

export function teardown(data) {
    console.log("Teste completo.");
}