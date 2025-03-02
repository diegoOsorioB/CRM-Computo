<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Registro</title>

        <!-- Bootstrap 5 -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">

        <!-- Estilos Personalizados -->
        <style>
            body {
                background: linear-gradient(to right, #74ebd5, #acb6e5);
                display: flex;
                justify-content: center;
                align-items: center;
                height: 100vh;
            }

            .container {
                max-width: 500px;
                background: white;
                padding: 30px;
                border-radius: 10px;
                box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
            }

            .hidden {
                display: none;
            }

            .fade {
                opacity: 0;
                transition: opacity 0.5s ease-in-out;
            }
        </style>

        <!-- JavaScript para manejar la visibilidad -->
        <script>
            function mostrarDomicilio() {
                document.getElementById('datos-basicos').classList.add('fade');
                setTimeout(() => {
                    document.getElementById('datos-basicos').classList.add('hidden');
                    document.getElementById('domicilio-section').classList.remove('hidden');
                    setTimeout(() => document.getElementById('domicilio-section').classList.remove('fade'), 50);
                }, 500);
            }

            function ocultarDomicilio() {
                document.getElementById('domicilio-section').classList.add('fade');
                setTimeout(() => {
                    document.getElementById('domicilio-section').classList.add('hidden');
                    document.getElementById('datos-basicos').classList.remove('hidden');
                    setTimeout(() => document.getElementById('datos-basicos').classList.remove('fade'), 50);
                }, 500);
            }

            function formatCardNumber(input) {
                let value = input.value.replace(/\D/g, '');
                value = value.replace(/(.{4})/g, '$1 ').trim();
                input.value = value;
            }
        </script>
    </head>
    <body>

        <div class="container">
            <h2 class="text-center mb-4">Registro</h2>
            <form action="RegisterServlet" method="post">

                <!-- Datos Básicos -->
                <div id="datos-basicos">
                    <div class="mb-3">
                        <label class="form-label">Nombre:</label>
                        <input type="text" class="form-control" name="nombre" required>
                    </div>

                    <div class="mb-3">
                        <label class="form-label">Apellido:</label>
                        <input type="text" class="form-control" name="apellido" required>
                    </div>

                    <div class="mb-3">
                        <label class="form-label">Usuario:</label>
                        <input type="text" class="form-control" name="username" required>
                    </div>

                    <div class="mb-3">
                        <label class="form-label">Contraseña:</label>
                        <input type="password" class="form-control" name="password" required>
                    </div>

                    <!-- Botón para avanzar -->
                    <button type="button" class="btn btn-primary w-100" onclick="mostrarDomicilio()">Avanzar</button>
                </div>

                <!-- Sección de Domicilio (Oculta inicialmente) -->
                <div id="domicilio-section" class="hidden fade" style="padding-top: 20px;">
                    <h4 class="text-center mt-4">Domicilio</h4>

                    <div class="mb-3">
                        <label class="form-label">Calle y número:</label>
                        <input type="text" class="form-control" id="street" name="street" required>
                    </div>

                    <div class="mb-3">
                        <label class="form-label">Colonia/Barrio:</label>
                        <input type="text" class="form-control" id="neighborhood" name="neighborhood" required>
                    </div>

                    <div class="mb-3">
                        <label class="form-label">Código Postal:</label>
                        <input type="text" class="form-control" id="postal-code" name="postal-code" maxlength="10" required>
                    </div>

                    <div class="mb-3">
                        <label class="form-label">Ciudad:</label>
                        <input type="text" class="form-control" id="city" name="city" required>
                    </div>

                    <div class="mb-3">
                        <label class="form-label">Estado/Provincia:</label>
                        <input type="text" class="form-control" id="state" name="state" required>
                    </div>

                    <div class="mb-3">
                        <label class="form-label">País:</label>
                        <select class="form-select" id="country" name="country" required>
                            <option value="" disabled selected>Selecciona tu país</option>
                            <option value="mx">México</option>
                            <option value="us">Estados Unidos</option>
                            <option value="es">España</option>
                            <option value="ar">Argentina</option>
                            <option value="co">Colombia</option>
                            <option value="cl">Chile</option>
                            <option value="pe">Perú</option>
                            <option value="other">Otro</option>
                        </select>
                    </div>

                    <!-- Número de Tarjeta -->
                    <h4 class="text-center mt-4">Información de Pago</h4>
                    <div class="mb-3">
                        <label class="form-label">Número de Tarjeta:</label>
                        <input type="text" class="form-control" id="card-number" name="card-number" maxlength="19" oninput="formatCardNumber(this)" required>
                    </div>

                    <!-- Botón de Registro -->
                    <button type="submit" class="btn btn-success w-100">Registrarse</button>

                    <!-- Botón para regresar -->
                    <button type="button" class="btn btn-secondary w-100 mt-2" onclick="ocultarDomicilio()">Regresar</button>
                </div>

            </form>

        </div>

        <!-- Bootstrap JS -->
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    </body>
</html>
