@echo off
rem Sobe o Keycloak em modo de desenvolvimento na porta 8180, com o realm do Caminho Remoto.
rem O Keycloak fica em ..\..\keycloak (fora dos repositórios), baixado de github.com/keycloak/keycloak.
rem SOMENTE DESENVOLVIMENTO: o console de administração usa usuário "admin" e senha "admin".

setlocal
set PASTA_DO_KEYCLOAK=%~dp0..\..\keycloak

if not exist "%PASTA_DO_KEYCLOAK%\bin\kc.bat" (
  echo Keycloak nao encontrado em %PASTA_DO_KEYCLOAK%. Veja o README da API.
  exit /b 1
)

if not exist "%PASTA_DO_KEYCLOAK%\data\import" mkdir "%PASTA_DO_KEYCLOAK%\data\import"
copy /Y "%~dp0realm-caminho-remoto.json" "%PASTA_DO_KEYCLOAK%\data\import\" > nul

set KC_BOOTSTRAP_ADMIN_USERNAME=admin
set KC_BOOTSTRAP_ADMIN_PASSWORD=admin

call "%PASTA_DO_KEYCLOAK%\bin\kc.bat" start-dev --http-port=8180 --import-realm
