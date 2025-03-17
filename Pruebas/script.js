let chatMensajes = [];
let calificaciones = [];
let tickets = {
    "12345": "Pendiente",
    "67890": "Resuelto"
};

// Respuestas automáticas predefinidas
const respuestasAutomáticas = {
    "hola": "¡Hola! Bienvenido, ¿en qué puedo ayudarte hoy?",
    "horarios": "Nuestro horario de atención es de lunes a viernes, de 9:00 AM a 6:00 PM.",
    "devoluciones": "Tienes 15 días para devolver un producto a partir de la fecha de compra.",
    "envios": "Los envíos se realizan de lunes a viernes y pueden tardar entre 2 a 5 días hábiles.",
    "contacto": "Puedes contactarnos a través del correo contacto@empresa.com.",
    "ayuda": "Por supuesto, ¿en qué tema necesitas ayuda? Puedo brindarte información sobre horarios, devoluciones, productos, etc.",
    "productos": "Consulta nuestro catálogo de productos en nuestra página web.",
    "pedidos": "Puedes ver el estado de tu pedido en la sección 'Seguimiento de Ticket'.",
    "soporte": "Nuestro equipo de soporte está disponible para ayudarte con cualquier inconveniente.",
    "gracias": "¡Gracias por contactarnos! Si necesitas algo más, no dudes en preguntar."
};

// Función para enviar el mensaje del usuario y obtener la respuesta automática
function enviarMensajeChat() {
    const userInput = document.getElementById('userInput').value.trim().toLowerCase();
    
    if (userInput) {
        chatMensajes.push("Cliente: " + userInput);
        let respuesta = respuestasAutomáticas[userInput] || "Lo siento, no entendí tu mensaje. ¿Podrías repetirlo?";
        chatMensajes.push("Asistente: " + respuesta);
        document.getElementById('userInput').value = "";
        actualizarChatbox();
    }
}

// Actualizar el chat con los nuevos mensajes
function actualizarChatbox() {
    const chatbox = document.getElementById('chatbox');
    chatbox.innerHTML = chatMensajes.join('<br>');
    chatbox.scrollTop = chatbox.scrollHeight;  // Desplazamiento automático hacia abajo
}

function enviarFormulario() {
    const nombre = document.getElementById('nombre').value;
    const email = document.getElementById('email').value;
    const mensaje = document.getElementById('mensaje').value;

    if (nombre && email && mensaje) {
        alert("Formulario enviado\nNombre: " + nombre + "\nCorreo: " + email + "\nMensaje: " + mensaje);
        // Aquí podrías realizar la lógica de envío al servidor o base de datos
    } else {
        alert("Por favor, complete todos los campos.");
    }
}

function consultarTicket() {
    const numeroTicket = document.getElementById('numeroTicket').value;
    const ticketStatus = document.getElementById('ticketStatus');

    if (tickets[numeroTicket]) {
        ticketStatus.textContent = "Estado: " + tickets[numeroTicket];
    } else {
        ticketStatus.textContent = "Estado: Ticket no encontrado.";
    }
}

function enviarCalificacion() {
    const calificacion = parseInt(document.getElementById('calificacion').value);
    calificaciones.push(calificacion);

    const promedio = calificaciones.reduce((a, b) => a + b, 0) / calificaciones.length;
    document.getElementById('averageRating').textContent = "Calificación promedio: " + promedio.toFixed(1);
}
