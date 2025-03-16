document.addEventListener("DOMContentLoaded", () => {
    const chatbox = document.getElementById("chatbox");
    const userInput = document.getElementById("userInput");
    const sendBtn = document.getElementById("sendBtn");

    // Chatbot
    sendBtn.addEventListener("click", () => {
        let userMessage = userInput.value.trim();
        if (userMessage === "") return;

        chatbox.insertAdjacentHTML("beforeend", `<p><strong>Tú:</strong> ${userMessage}</p>`);
        userInput.value = "";

        setTimeout(() => {
            let botResponse = getBotResponse(userMessage);
            chatbox.insertAdjacentHTML("beforeend", `<p><strong>Bot:</strong> ${botResponse}</p>`);
            chatbox.scrollTop = chatbox.scrollHeight; // Auto-scroll
        }, 1000);
    });

    function getBotResponse(message) {
        const responses = {
            "hola": "¡Hola! ¿Cómo puedo ayudarte?",
            "horarios": "Nuestro horario es de lunes a sábado de 8:00 AM a 8:00 PM.",
            "devoluciones": "Las devoluciones se pueden realizar dentro de los 30 días posteriores a la compra con el ticket original."
        };
        return responses[message.toLowerCase()] || "Lo siento, no entiendo tu pregunta.";
    }

    // Formulario de contacto
    const contactForm = document.getElementById("contactForm");
    contactForm.addEventListener("submit", (e) => {
        e.preventDefault();
        
        // Validación básica
        if (document.getElementById("nombre").value && document.getElementById("email").value && document.getElementById("mensaje").value) {
            document.getElementById("confirmMessage").style.display = "block";
        } else {
            alert("Por favor, completa todos los campos.");
        }
    });

    // Seguimiento de ticket
    document.getElementById("trackBtn").addEventListener("click", () => {
        const ticketInput = document.getElementById("ticketInput").value.trim();
        document.getElementById("ticketStatus").textContent = ticketInput
            ? `El ticket #${ticketInput} está en proceso.`
            : "Ingrese un número de ticket válido.";
    });

    // Evaluación del servicio
    document.getElementById("rateBtn").addEventListener("click", () => {
        const rating = document.getElementById("rating").value;
        let previousRating = localStorage.getItem("userRating");
        
        // Calcular promedio de calificaciones
        if (previousRating) {
            const totalRatings = parseInt(localStorage.getItem("totalRatings") || "0");
            const totalStars = parseInt(localStorage.getItem("totalStars") || "0");
            const newTotalStars = totalStars + parseInt(rating);
            const newTotalRatings = totalRatings + 1;
            const averageRating = newTotalStars / newTotalRatings;

            localStorage.setItem("totalStars", newTotalStars);
            localStorage.setItem("totalRatings", newTotalRatings);

            document.getElementById("averageRating").textContent = `Gracias por tu calificación de ${rating} estrellas. Calificación promedio: ${averageRating.toFixed(2)} estrellas.`;
        } else {
            localStorage.setItem("userRating", rating);
            localStorage.setItem("totalRatings", "1");
            localStorage.setItem("totalStars", rating);
            document.getElementById("averageRating").textContent = `Gracias por tu calificación de ${rating} estrellas.`;
        }
    });
});
