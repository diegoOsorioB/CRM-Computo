<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login</title>
    <link rel="stylesheet" href="style/styles.css"/>
</head>
<body>
    <div class="login-container">
        <h2 class="login-title">Bienvenido</h2>
        <form action="LoginServlet" method="post" class="login-form">
            <div class="input-group">
                <label for="username">Nombre de usuario:</label>
                <input type="text" id="username" name="username" required placeholder="Introduce tu usuario">
            </div>
            <div class="input-group">
                <label for="password">Contraseña:</label>
                <input type="password" id="password" name="password" required placeholder="Introduce tu contraseña">
            </div>
            <div class="submit-btn">
                <input type="submit" value="Iniciar sesión">
            </div>
            <div class="registroC">
            <a href="">¿Olvidaste tu contraseña?</a>
            </div>
        </form>

        <!-- Botón para redirigir al registro -->
        <div class="submit-btn2">
            <button type="button" class="submit-btn2" onclick="window.location.href='RegisterForm.jsp'">Registrarse</button>
        </div>

            </div>
        </form>
    </div>
</body>
</html>
