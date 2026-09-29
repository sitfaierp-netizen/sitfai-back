import os
import re

test_dir = r"c:\ERP_CORE\Api_Tienda\Api_Tienda\src\test\java\com\SITFAI_CORE_ERP_TIENDA"
target_annotations = [
    r'@(org\.springframework\.boot\.test\.context\.)?SpringBootTest(\([^)]*\))?',
    r'@(org\.springframework\.test\.context\.)?ActiveProfiles(\([^)]*\))?',
    r'@(org\.springframework\.context\.annotation\.)?Import(\([^)]*\))?',
    r'@(org\.springframework\.boot\.test\.autoconfigure\.web\.servlet\.)?AutoConfigureMockMvc(\([^)]*\))?',
    r'@(org\.springframework\.test\.context\.)?ContextConfiguration(\([^)]*\))?'
]

for root, _, files in os.walk(test_dir):
    for f in files:
        if f.endswith("Test.java") or f.endswith("IT.java"):
            path = os.path.join(root, f)
            with open(path, 'r', encoding='utf-8') as file:
                content = file.read()
                
            # No modificar la propia clase AbstractIntegrationTest
            if f == "AbstractIntegrationTest.java":
                continue
                
            original_content = content
            
            # Remover anotaciones
            for ann in target_annotations:
                content = re.sub(ann + r'\s*', '', content)
                
            if content != original_content:
                with open(path, 'w', encoding='utf-8') as file:
                    file.write(content)
                print(f"Cleaned more annotations in: {f}")
