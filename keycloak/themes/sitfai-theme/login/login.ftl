<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Iniciar Sesi&oacute;n - SITFAI ERP</title>
    <link rel="stylesheet" href="${url.resourcesPath}/css/login.css">
</head>
<body>
    <div class="login-container">
        <!-- Left Panel -->
        <div class="login-left">
            <div class="logo">
                <span class="material-symbols-outlined">layers</span>
                SITFAI ERP
            </div>
            
            <h1>Gesti&oacute;n Inteligente para la Empresa Moderna</h1>
            <p>
                Unifique sus operaciones, simplifique sus finanzas y escale su negocio con nuestra plataforma integral dise&ntilde;ada para la alta eficiencia.
            </p>
        </div>

        <!-- Right Panel -->
        <div class="login-right">
            <div class="login-form-wrapper">
                <h2>Iniciar Sesi&oacute;n</h2>
                <p class="subtitle">Ingrese sus credenciales corporativas.</p>

                <#if message?has_content && (message.type != 'warning' || !isAppInitiatedAction??)>
                    <div class="alert-error">
                        <span class="kc-feedback-text">${kcSanitize(message.summary)?no_esc}</span>
                    </div>
                </#if>

                <form onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
                    
                    <div class="form-group">
                        <label for="username">Correo Electr&oacute;nico</label>
                        <div class="input-icon-wrapper">
                            <span class="material-symbols-outlined">mail</span>
                            <input tabindex="1" id="username" name="username" value="${(login.username!'')}" type="text" autofocus autocomplete="off" placeholder="admin@sitfai.com" />
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="password">Contrase&ntilde;a</label>
                        <div class="input-icon-wrapper">
                            <span class="material-symbols-outlined">lock</span>
                            <input tabindex="2" id="password" name="password" type="password" autocomplete="off" placeholder="********" />
                        </div>
                    </div>

                    <div class="form-actions">
                        <label class="remember-me">
                            <#if login.rememberMe??>
                                <input tabindex="3" id="rememberMe" name="rememberMe" type="checkbox" checked> Recordarme
                            <#else>
                                <input tabindex="3" id="rememberMe" name="rememberMe" type="checkbox"> Recordarme
                            </#if>
                        </label>
                        <#if realm.resetPasswordAllowed>
                            <a tabindex="5" href="${url.loginResetCredentialsUrl}" class="forgot-password">&iquest;Olvid&oacute; su contrase&ntilde;a?</a>
                        </#if>
                    </div>

                    <div id="kc-form-buttons">
                        <input tabindex="4" name="login" id="kc-login" type="submit" class="btn-primary" value="Ingresar al Sistema" />
                    </div>
                </form>
            </div>
        </div>
    </div>
</body>
</html>
