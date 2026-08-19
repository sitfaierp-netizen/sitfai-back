<#macro registrationLayout bodyClass="" displayInfo=false displayMessage=true displayRequiredFields=false>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SITFAI ERP - Iniciar SesiÃƒÂ³n</title>
    <link rel="icon" href="${url.resourcesPath}/img/favicon.ico" />
    <script src="https://unpkg.com/@tailwindcss/browser@4"></script>
</head>
<body class="m-0 p-0 bg-slate-50 font-sans">
    <!-- Alertas flotantes (Errores, ÃƒÂ©xito, etc.) -->
    <#if displayMessage && message?has_content && (message.type != 'warning' || !isAppInitiatedAction??)>
        <div class="absolute top-4 right-4 z-50 p-4 rounded-md shadow-lg text-white font-bold max-w-sm
            <#if message.type = 'success'>bg-green-500
            <#elseif message.type = 'warning'>bg-yellow-500
            <#elseif message.type = 'error'>bg-red-500
            <#else>bg-blue-500</#if>">
            ${kcSanitize(message.summary)?no_esc}
        </div>
    </#if>

    <!-- AquÃƒÂ­ se inyecta nuestro login.ftl -->
    <#nested "form">
</body>
</html>
</#macro>