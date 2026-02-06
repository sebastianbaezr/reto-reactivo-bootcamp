package co.com.bancolombia.model.bootcamp.gateways;

public interface BootcampReportGateway {
    /**
     * Envía el reporte del bootcamp de forma asincrónica sin bloquear el flujo principal.
     * @param bootcampId id del bootcamp registrado
     * @param capacityCount cantidad de capacidades
     * @param technologyCount cantidad de tecnologías
     */
    void sendBootcampReportAsync(Long bootcampId, Long capacityCount, Long technologyCount);
}
