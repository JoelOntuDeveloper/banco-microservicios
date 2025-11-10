package banco.jontuna.ms_banco_cliente.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import banco.jontuna.ms_banco_cliente.config.RabbitMQConfig;
import banco.jontuna.ms_banco_cliente.event.ClienteCreadoEvent;

@Service
public class ClienteEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(ClienteEventPublisher.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publicarClienteCreado(Long clienteId, String nombre, String identificacion) {
        try {
            ClienteCreadoEvent evento = new ClienteCreadoEvent(clienteId, nombre, identificacion);
            
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                evento
            );
            
            logger.info("Evento ClienteCreadoEvent publicado exitosamente para cliente ID: {}", clienteId);
            
        } catch (Exception e) {
            logger.error("Error al publicar evento ClienteCreadoEvent para cliente ID: {}", clienteId, e);
        }
    }
}