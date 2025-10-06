package com.pigeonpulse.controller;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.pigeonpulse.model.Paloma;
import com.pigeonpulse.model.Palomar;
import com.pigeonpulse.model.Usuario;
import com.pigeonpulse.security.PalomarContext;
import com.pigeonpulse.service.PalomaService;
import com.pigeonpulse.service.PalomarService;
import com.pigeonpulse.service.UsuarioPalomarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reportes")
@Tag(name = "Reportes", description = "API para reportes y estadísticas")
public class ReportesController {

    @Autowired
    private PalomaService palomaService;

    @Autowired
    private UsuarioPalomarService usuarioPalomarService;

    @Autowired
    private PalomarService palomarService;

    @GetMapping("/estadisticas")
    @Operation(summary = "Obtener estadísticas generales")
    public ResponseEntity<Map<String, Object>> getEstadisticas(@RequestParam(required = false) String palomarId) throws ExecutionException, InterruptedException {
        PalomarContext palomarContext = (PalomarContext) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Usuario usuario = palomarContext.getUsuario();

        // Use provided palomarId or default to JWT context
        String targetPalomarId = palomarId != null ? palomarId : palomarContext.getPalomarId();

        // Verify user has access to the target palomar
        boolean hasAccess = usuarioPalomarService.hasAccessToPalomar(usuario.getId(), targetPalomarId);
        if (!hasAccess) {
            return ResponseEntity.status(403).build();
        }

        List<Paloma> palomas = palomaService.findByPalomarId(targetPalomarId);

        long total = palomas.size();
        long enCarrera = palomas.stream().filter(p -> "Activa en carrera".equals(p.getEstado())).count();
        long reproductoras = palomas.stream().filter(p -> "Reproductora".equals(p.getEstado())).count();
        long otras = palomas.stream().filter(p -> "Otra".equals(p.getEstado())).count();

        Map<String, Long> porSexo = palomas.stream()
                .filter(p -> p.getSexo() != null)
                .collect(Collectors.groupingBy(Paloma::getSexo, Collectors.counting()));

        Map<String, Long> porLinea = palomas.stream()
                .filter(p -> p.getLinea() != null)
                .collect(Collectors.groupingBy(Paloma::getLinea, Collectors.counting()));

        Map<String, Object> estadisticas = Map.of(
            "totalPalomas", total,
            "palomasEnCarrera", enCarrera,
            "palomasReproductoras", reproductoras,
            "palomasOtras", otras,
            "palomasPorLinea", porLinea,
            "palomasPorSexo", porSexo
        );

        return ResponseEntity.ok(estadisticas);
    }

    @PostMapping("/censo/pdf")
    @Operation(summary = "Generar censo en PDF")
    public ResponseEntity<byte[]> generarCensoPDF(@RequestBody CensoRequest request) throws ExecutionException, InterruptedException, IOException {
        PalomarContext palomarContext = (PalomarContext) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Usuario usuario = palomarContext.getUsuario();

        // Use provided palomarId or default to JWT context
        String targetPalomarId = request.palomarId() != null ? request.palomarId() : palomarContext.getPalomarId();

        // Verify user has access to the target palomar
        boolean hasAccess = usuarioPalomarService.hasAccessToPalomar(usuario.getId(), targetPalomarId);
        if (!hasAccess) {
            return ResponseEntity.status(403).build();
        }

        // Get palomar info for header
        Palomar palomar = palomarService.findById(targetPalomarId).orElseThrow();

        // Get filtered palomas
        List<Paloma> palomas = getFilteredPalomas(targetPalomarId, request);

        // Generate PDF
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        // Header
        document.add(new Paragraph("Documento generado por PalomaApp")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(16)
                .setBold());

        document.add(new Paragraph("Censo de Palomas")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(14)
                .setBold());

        document.add(new Paragraph("Palomar: " + palomar.getNombre())
                .setTextAlignment(TextAlignment.LEFT));

        document.add(new Paragraph("Propietario: " + request.nombrePropietario())
                .setTextAlignment(TextAlignment.LEFT));

        if (request.telefono() != null && !request.telefono().isEmpty()) {
            document.add(new Paragraph("Teléfono: " + request.telefono())
                    .setTextAlignment(TextAlignment.LEFT));
        }

        if (request.domicilio() != null && !request.domicilio().isEmpty()) {
            document.add(new Paragraph("Domicilio: " + request.domicilio())
                    .setTextAlignment(TextAlignment.LEFT));
        }

        document.add(new Paragraph("Fecha de generación: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .setTextAlignment(TextAlignment.LEFT));

        document.add(new Paragraph(" ")); // Empty line

        // Create table
        Table table = new Table(UnitValue.createPercentArray(new float[]{8, 6, 8, 8, 8, 8, 8, 8, 8}))
                .useAllAvailableWidth();

        // Headers
        table.addHeaderCell("Anillo");
        table.addHeaderCell("Año");
        table.addHeaderCell("Sexo");
        table.addHeaderCell("Color");
        table.addHeaderCell("Línea");
        table.addHeaderCell("Estado");
        table.addHeaderCell("Padre");
        table.addHeaderCell("Madre");
        table.addHeaderCell("Observaciones");

        // Data rows
        for (Paloma paloma : palomas) {
            table.addCell(paloma.getAnillo() != null ? paloma.getAnillo() : "");
            table.addCell(paloma.getAño() != null ? paloma.getAño().toString() : "");
            table.addCell(paloma.getSexo() != null ? paloma.getSexo() : "");
            table.addCell(paloma.getColor() != null ? paloma.getColor() : "");
            table.addCell(paloma.getLinea() != null ? paloma.getLinea() : "");
            table.addCell(paloma.getEstado() != null ? paloma.getEstado() : "");
            table.addCell(paloma.getPadre() != null ? paloma.getPadre() : "");
            table.addCell(paloma.getMadre() != null ? paloma.getMadre() : "");
            table.addCell(paloma.getObservaciones() != null ? paloma.getObservaciones() : "");
        }

        document.add(table);

        // Footer
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Total de palomas: " + palomas.size())
                .setTextAlignment(TextAlignment.LEFT));

        document.close();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "censo_palomas.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(baos.toByteArray());
    }


    public record Filtros(
        Integer año,
        String linea,
        String estado
    ) {}

    public record CensoRequest(
        String nombrePropietario,
        String telefono,
        String domicilio,
        Filtros filtros,
        String palomarId
    ) {}

    private List<Paloma> getFilteredPalomas(String palomarId, CensoRequest request) throws ExecutionException, InterruptedException {
        List<Paloma> palomas = palomaService.findByPalomarId(palomarId);

        // Apply filters from nested filtros object
        if (request.filtros() != null) {
            if (request.filtros().año() != null) {
                palomas = palomas.stream()
                        .filter(p -> request.filtros().año().equals(p.getAño()))
                        .collect(Collectors.toList());
            }

            if (request.filtros().estado() != null && !request.filtros().estado().isEmpty()) {
                palomas = palomas.stream()
                        .filter(p -> request.filtros().estado().equals(p.getEstado()))
                        .collect(Collectors.toList());
            }

            if (request.filtros().linea() != null && !request.filtros().linea().isEmpty()) {
                palomas = palomas.stream()
                        .filter(p -> request.filtros().linea().equals(p.getLinea()))
                        .collect(Collectors.toList());
            }
        }

        return palomas;
    }
}