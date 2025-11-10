package banco.jontuna.ms_banco_cuenta.consumer;

import banco.jontuna.ms_banco_cuenta.event.ClienteCreadoEvent;
import banco.jontuna.ms_banco_cuenta.service.CuentaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ClienteCreadoConsumer {

    private static final Logger logger = LoggerFactory.getLogger(ClienteCreadoConsumer.class);

    @Autowired
    private CuentaService cuentaService;

    @RabbitListener(queues = "clientes.queue")
    public void recibirClienteCreado(ClienteCreadoEvent evento) {
        try {
            logger.info("Evento recibido - Creando cuenta automática para cliente: {}", evento.getClienteId());
            
            boolean clienteTieneCuentas = !cuentaService.obtenerCuentasPorCliente(evento.getClienteId()).isEmpty();
            
            if (clienteTieneCuentas) {
                logger.warn("El cliente ID: {} ya tiene cuentas registradas. No se creará cuenta automática.", 
                            evento.getClienteId());
                return;
            }
            
            var cuentaCreada = cuentaService.crearCuentaAutomatica(evento.getClienteId(), evento.getNombre());
            
            if (cuentaCreada != null) {
                logger.info("Cuenta automática creada exitosamente: {} para cliente ID: {}", 
                            cuentaCreada.getNumeroCuenta(), evento.getClienteId());
            } else {
                logger.error("No se pudo crear la cuenta automática para cliente ID: {}", evento.getClienteId());
            }
            
        } catch (Exception e) {
            logger.error("Error al procesar evento ClienteCreadoEvent para cliente ID: {}", 
                        evento.getClienteId(), e);
        }
    }
}