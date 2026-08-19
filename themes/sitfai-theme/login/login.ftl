<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('username','password') displayInfo=realm.password && realm.registrationAllowed && !registrationDisabled??; section>
    <#if section = "header">
        Inicia Sesión
    <#elseif section = "form">
        <div class="min-h-screen bg-slate-50 flex font-sans" style="background-color: #f8fafc;">
          <!-- Left Branding Panel -->
          <div class="hidden lg:flex lg:w-1/2 relative overflow-hidden flex-col justify-between p-12" style="background-color: #0f172a;">
            <div class="relative z-10">
              <div class="flex items-center text-white font-bold text-2xl tracking-tight mb-12">
                 <svg class="w-8 h-8 mr-3" style="color: #3b82f6;" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M12 2L2 7l10 5 10-5-10-5z" fill="currentColor"/><path d="M2 17l10 5 10-5M2 12l10 5 10-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
                 SITFAI ERP
              </div>
              <h1 class="text-4xl lg:text-5xl font-extrabold text-white leading-tight max-w-md tracking-tight">
                Gestión Inteligente para la Empresa Moderna
              </h1>
              <p class="mt-6 text-slate-300 text-lg max-w-md leading-relaxed">
                Unifique sus operaciones, simplifique sus finanzas y escale su negocio con nuestra plataforma integral diseñada para la alta eficiencia.
              </p>
            </div>
            
            <div class="relative z-10 text-slate-500 text-sm font-medium">
              &copy; 2026 SITFAI Technologies. Todos los derechos reservados.
            </div>
          </div>

          <!-- Right Login Panel -->
          <div class="w-full lg:w-1/2 flex flex-col justify-center items-center p-8 sm:p-12 md:p-24 bg-white relative shadow-2xl z-10">
            <div class="w-full max-w-sm space-y-8">
              
              <div class="text-center lg:text-left">
                <h2 class="text-2xl font-bold text-slate-900 tracking-tight">Iniciar Sesión</h2>
                <p class="mt-2 text-sm text-slate-500">Ingrese sus credenciales corporativas.</p>
              </div>

              <!-- EL FORMULARIO NATIVO DE KEYCLOAK -->
              <form class="mt-8 space-y-6" id="kc-form-login" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
                <div class="space-y-5">
                  <div>
                    <label for="username" class="block text-sm font-medium text-slate-700 mb-1">Correo Electrónico o Usuario</label>
                    <div class="relative">
                      <input id="username" name="username" value="${(login.username!'')}" type="text" autocomplete="off" required class="block w-full pl-3 pr-3 py-2.5 border border-slate-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 sm:text-sm text-slate-900 bg-slate-50 focus:bg-white" placeholder="admin@sitfai.com">
                    </div>
                  </div>

                  <div>
                    <label for="password" class="block text-sm font-medium text-slate-700 mb-1">Contraseña</label>
                    <div class="relative">
                      <input id="password" name="password" type="password" autocomplete="off" required class="block w-full pl-3 pr-3 py-2.5 border border-slate-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 sm:text-sm text-slate-900 bg-slate-50 focus:bg-white" placeholder="••••••••">
                    </div>
                  </div>
                </div>

                <div class="flex items-center justify-between">
                  <#if realm.rememberMe && !usernameHidden??>
                  <div class="flex items-center">
                    <input id="rememberMe" name="rememberMe" type="checkbox" class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-slate-300 rounded cursor-pointer" <#if login.rememberMe??>checked</#if>>
                    <label for="rememberMe" class="ml-2 block text-sm text-slate-700 cursor-pointer">
                      Recordarme
                    </label>
                  </div>
                  </#if>
                </div>

                <div>
                  <button type="submit" id="kc-login" class="w-full flex justify-center py-2.5 px-4 border border-transparent rounded-md shadow-sm text-sm font-bold text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-colors cursor-pointer" style="background-color: #2563eb;">
                    Ingresar al Sistema
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
    </#if>
</@layout.registrationLayout>