const fs = require('fs');
const file = '../sitfai-front/src/features/empresas/empresas-page.vue';
let content = fs.readFileSync(file, 'utf8');

// 1. Get mutation from useEmpresas
content = content.replace(
  /const \{ obtenerEmpresas, crearEmpresaMutation \} = useEmpresas\(\);/,
  'const { obtenerEmpresas, crearEmpresaMutation, eliminarEmpresaMutation } = useEmpresas();'
);

// 2. Replace manejarAccionPendiente with eliminar function
const newFunction = `
const eliminarEmpresa = async (empresaId: string) => {
  if (!confirm('¿Estás seguro de eliminar esta empresa?')) return;
  try {
    await eliminarEmpresaMutation.mutateAsync(empresaId);
    toast.success('Empresa eliminada correctamente');
  } catch (error: any) {
    toast.error('Error al eliminar empresa');
  }
};
`;
content = content.replace(
  /const manejarAccionPendiente = \(\) => \{[\s\S]*?\};\n/,
  newFunction
);

// 3. Replace the button action in template
content = content.replace(
  /@click\.prevent="manejarAccionPendiente" class="text-slate-400 hover:text-blue-600 p-1 cursor-pointer">\s*<span class="material-symbols-outlined text-\[18px\]">edit<\/span>/,
  '@click.prevent="eliminarEmpresa(empresa.id)" class="text-red-400 hover:text-red-600 p-1 cursor-pointer">\n                    <span class="material-symbols-outlined text-[18px]">delete</span>'
);

fs.writeFileSync(file, content, 'utf8');
