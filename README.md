# Basic config
## config.yaml
```yaml
mqtt:
  host: localhost
  port: 1883
  protocol: tcp
  username: mqtt_user
  password: password
  automatic_reconnect: true
  connection_timeout: 10

telegram:
  chat_id: "-1000000000000"
  bot_token: "0000000000:AAAbO1-dysu3daUTpjVEXX4Pwq-M9wrPT3"

listeners:
  matches:
    queues:
      - name: unknown
        message: "{{camName}}: Stranger detected."
      - name: jhon
        message: "{{camName}}: Jhon was identified."
  cameras:
    queues:
      - name: back-door
        message: "{{camName}}: {{personName}} identified."
      - name: front-door
        message: "{{camName}}: {{personName}} identified."
      - name: hallway
        message: "{{camName}}: {{personName}} identified."
```

## Run

### Docker run

```CMD
docker run icaroerasmo/double-take-telegram-notifier:0.0.1 \
    -v ./double-take-telegram-notifier:/app/config
```

### Docker compose

```yaml
version: "3.9"
services:
  telegram-notifier:
    container_name: telegram-notifier
    image: ghcr.io/icaroerasmo/double-take-telegram-notifier:release-0.0.1
    restart: always
    volumes:
      - ./telegram-notifier:/app/config
networks:
  default:
    name: frigate-network
```
