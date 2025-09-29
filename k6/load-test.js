import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

// Métricas customizadas
export let errorRate = new Rate('errors');

// Configuração do teste
export let options = {
    stages: [
        { duration: '2m', target: 10 },
        { duration: '5m', target: 10 },
        { duration: '2m', target: 20 },
        { duration: '5m', target: 20 },
        { duration: '2m', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<1000'], // 95% das requisições devem ser < 500ms
        http_req_failed: ['rate<0.1'],    // Taxa de erro < 10%
        errors: ['rate<0.1'],
    },
};

// Configuração da API
const BASE_URL = __ENV.API_URL || 'https://taskflow-api-1014288660691.us-central1.run.app';
const API_ENDPOINTS = {
    auth: `${BASE_URL}/auth`,
    tasks: `${BASE_URL}/tasks`,
    register: `${BASE_URL}/auth/signup`
};

// Dados de teste
const testUsers = [
    { username: 'user1', password: 'password123', email: 'user1@test.com' },
    { username: 'user2', password: 'password123', email: 'user2@test.com' },
    { username: 'user3', password: 'password123', email: 'user3@test.com' },
];

let authTokens = [];

export function setup() {
    // Registrar usuários de teste
    testUsers.forEach(user => {
        const registerPayload = {
            name: user.username,
            password: user.password,
            email: user.email
        };

        http.post(`${API_ENDPOINTS.register}`, JSON.stringify(registerPayload), {
            headers: { 'Content-Type': 'application/json' },
        });
    });

    // Fazer login e obter tokens
    const tokens = [];
    testUsers.forEach(user => {
        const loginPayload = {
            name: user.username,
            password: user.password
        };

        const response = http.post(`${API_ENDPOINTS.auth}/login`, JSON.stringify(loginPayload), {
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
        const response = http.get(API_ENDPOINTS.tasks, { headers });

        const success = check(response, {
            'GET /tasks status is 200': (r) => r.status === 200,
            'GET /tasks response time < 500ms': (r) => r.timings.duration < 500,
        });

        if (!success) {
            errorRate.add(1);
        }
    }

    // Cenário 2: Criar nova task (20% das requisições)
    else if (Math.random() < 0.9) {
        const taskPayload = {
            title: `Task ${Math.random().toString(36).substring(7)}`,
            description: `Description for task created at ${new Date().toISOString()}`,
            status: 'PENDING'
        };

        const response = http.post(API_ENDPOINTS.tasks, JSON.stringify(taskPayload), { headers });

        const success = check(response, {
            'POST /tasks status is 201': (r) => r.status === 201,
            'POST /tasks response time < 1000ms': (r) => r.timings.duration < 1000,
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
        const listResponse = http.get(API_ENDPOINTS.tasks, { headers });

        if (listResponse.status === 200) {
            try {
                const tasks = JSON.parse(listResponse.body);
                if (tasks.length > 0) {
                    const randomTask = tasks[Math.floor(Math.random() * tasks.length)];
                    const updatePayload = {
                        title: `Updated ${randomTask.title}`,
                        description: `Updated description at ${new Date().toISOString()}`,
                        status: Math.random() < 0.5 ? 'IN_PROGRESS' : 'COMPLETED'
                    };

                    const response = http.put(`${API_ENDPOINTS.tasks}/${randomTask.id}`,
                        JSON.stringify(updatePayload), { headers });

                    const success = check(response, {
                        'PUT /tasks/{id} status is 200': (r) => r.status === 200,
                        'PUT /tasks/{id} response time < 1000ms': (r) => r.timings.duration < 1000,
                    });

                    if (!success) {
                        errorRate.add(1);
                    }
                }
            } catch (e) {
                errorRate.add(1);
            }
        }
    }

    sleep(1); // Pausa de 1 segundo entre requisições
}

export function teardown(data) {
    console.log('Teste de carga finalizado');
}