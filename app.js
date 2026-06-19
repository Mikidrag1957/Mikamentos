const statusDiv = document.getElementById('status');
const setupSection = document.getElementById('setupSection');
const transferSection = document.getElementById('transferSection');
const btnCreate = document.getElementById('btnCreate');
const btnJoin = document.getElementById('btnJoin');
const roomInput = document.getElementById('roomInput');
const fileInput = document.getElementById('fileInput');
const btnSend = document.getElementById('btnSend');
const progressBar = document.getElementById('progressBar');

let peer = null;
let connection = null;

// Inicializar el nodo de red
peer = new Peer();

peer.on('open', (id) => {
    statusDiv.innerText = "Listo para conectar. Tu código es: " + id;
});

// ESCENARIO A: Crear sala y esperar al otro móvil
btnCreate.addEventListener('click', () => {
    const currentId = peer.id;
    statusDiv.innerText = "Esperando que el otro móvil se conecte al código: " + currentId;
    
    // Generar código QR visual para escanear con el otro teléfono
    document.getElementById('qrcode').innerHTML = "";
    new QRCode(document.getElementById('qrcode'), currentId);
});

// Recibir la conexión del otro móvil
peer.on('connection', (conn) => {
    connection = conn;
    configurarEventosConexion();
});

// ESCENARIO B: Conectarse al código del primer móvil
btnJoin.addEventListener('click', () => {
    const targetId = roomInput.value.trim();
    if (!targetId) return alert("Introduce un código válido");

    statusDiv.innerText = "Conectando vía Wi-Fi...";
    connection = peer.connect(targetId);
    configurarEventosConexion();
});

// Configurar la transferencia de datos una vez enlazados
function configurarEventosConexion() {
    connection.on('open', () => {
        statusDiv.innerText = "¡Conectados con éxito por Wi-Fi!";
        setupSection.classList.add('hidden');
        document.getElementById('qrcode').classList.add('hidden');
        transferSection.classList.remove('hidden');
    });

    // Recibir archivos desde el otro móvil
    connection.on('data', (data) => {
        if (data.type === 'file-start') {
            statusDiv.innerText = `Recibiendo archivo: ${data.name}...`;
            window.incomingChunks = [];
            window.incomingFileInfo = data;
        } else if (data.type === 'file-chunk') {
            window.incomingChunks.push(data.chunk);
            if (window.incomingChunks.length === window.incomingFileInfo.totalChunks) {
                // Reconstruir el archivo completo al terminar
                const blob = new Blob(window.incomingChunks);
                const url = URL.createObjectURL(blob);
                
                // Crear un enlace de descarga automática en Android
                const a = document.createElement('a');
                a.href = url;
                a.download = window.incomingFileInfo.name;
                a.click();
                
                statusDiv.innerText = "¡Archivo recibido y guardado!";
                window.incomingChunks = [];
            }
        }
    });
}

// Habilitar botón al cargar un archivo
fileInput.addEventListener('change', () => {
    if (fileInput.files.length > 0) btnSend.disabled = false;
});

// Enviar el archivo en bloques a través del canal de Wi-Fi
btnSend.addEventListener('click', () => {
    const file = fileInput.files[0];
    const chunkSize = 16384; // Bloques de 16KB optimizados para Wi-Fi
    const totalChunks = Math.ceil(file.size / chunkSize);
    
    // Avisar al receptor qué archivo se envía
    connection.send({
        type: 'file-start',
        name: file.name,
        totalChunks: totalChunks
    });

    let currentChunk = 0;
    progressBar.classList.remove('hidden');
    progressBar.value = 0;

    const readAndSendChunk = (offset) => {
        const reader = new FileReader();
        const slice = file.slice(offset, offset + chunkSize);
        
        reader.onload = (e) => {
            connection.send({
                type: 'file-chunk',
                chunk: e.target.result
            });
            
            currentChunk++;
            progressBar.value = (currentChunk / totalChunks) * 100;
            
            if (currentChunk < totalChunks) {
                readAndSendChunk(offset + chunkSize);
            } else {
                statusDiv.innerText = "¡Archivo enviado con éxito!";
                progressBar.classList.add('hidden');
            }
        };
        reader.readAsArrayBuffer(slice);
    };

    readAndSendChunk(0);
});
