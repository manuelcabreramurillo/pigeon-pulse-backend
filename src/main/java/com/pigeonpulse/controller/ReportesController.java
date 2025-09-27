package com.pigeonpulse.controller;

import com.pigeonpulse.model.Paloma;
import com.pigeonpulse.model.Usuario;
import com.pigeonpulse.security.PalomarContext;
import com.pigeonpulse.service.PalomaService;
import com.pigeonpulse.service.UsuarioPalomarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<byte[]> generarCensoPDF(@RequestBody CensoRequest request) {
        Usuario usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // TODO: Implementar generación de PDF con iText
        // Por ahora, devolver un placeholder
        return ResponseEntity.ok("PDF generation not implemented yet".getBytes());
    }

    @PostMapping("/censo/excel")
    @Operation(summary = "Generar censo en Excel")
    public ResponseEntity<byte[]> generarCensoExcel(@RequestBody CensoRequest request) {
        Usuario usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // TODO: Implementar generación de Excel con Apache POI
        // Por ahora, devolver un placeholder
        return ResponseEntity.ok("Excel generation not implemented yet".getBytes());
    }

    public record CensoRequest(
        String nombrePropietario,
        String telefono,
        String domicilio,
        String filtroAño,
        String filtroLinea,
        String filtroEstado
    ) {}
}