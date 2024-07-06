# Source the setenv.sh script
& ./setenv.sh

# Kill all java processes
Stop-Process -Name "java" -Force

# Sleep for 1 second
Start-Sleep -Seconds 1

# Start the Java application
Start-Process -FilePath "java" -ArgumentList "-jar attack_vector_2/backend/target/app.jar --server.port=443 --server.ssl.key-store=file:/home/ubuntu/keystore.p12 --server.ssl.key-store-password=password --server.ssl.key-store-type=pkcs12 --server.ssl.key-alias=tomcat --server.ssl.key-password=password" -RedirectStandardOutput "logs.txt" -RedirectStandardError "errors.txt" -NoNewWindow -PassThru

# Tail the logs.txt file
Get-Content "logs.txt" -Wait
