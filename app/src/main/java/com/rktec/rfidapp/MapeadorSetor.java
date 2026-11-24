package com.rktec.rfidapp;

import java.util.List;
import java.util.Map;

public class MapeadorSetor {

    /**
     * Normaliza item.codlocalizacao para o mesmo formato de código usado na lista de setores
     * (ex.: "001001" -> "1001"), para que a comparação com o setor selecionado funcione.
     * O campo continua guardando o CÓDIGO; o nome é obtido com {@link #nomeSetor(String)}.
     */
    public static void aplicar(List<ItemPlanilha> itens, Map<String, String> mapa) {
        if (itens == null || itens.isEmpty() || mapa == null || mapa.isEmpty()) return;

        for (ItemPlanilha item : itens) {
            if (item == null) continue;
            String original = safe(item.codlocalizacao);
            if (original.isEmpty()) continue;

            String chave = normalizeCodigo(original);
            String codigo = null;
            if (mapa.containsKey(chave)) {
                codigo = chave;
            } else {
                // tentativa com zeros à esquerda (ex.: width 3 / 2)
                String alt3 = addLeadingZeros(chave, 3);
                String alt2 = addLeadingZeros(chave, 2);
                if (mapa.containsKey(alt3)) codigo = alt3;
                else if (mapa.containsKey(alt2)) codigo = alt2;
            }
            if (codigo != null) {
                item.codlocalizacao = codigo; // mantém o CÓDIGO, só normalizado
            }
        }
    }

    /** Retorna o nome do setor para o código informado, ou o próprio código se não achar. */
    public static String nomeSetor(String codlocalizacao) {
        String cod = safe(codlocalizacao);
        if (cod.isEmpty()) return cod;
        List<SetorLocalizacao> setores = DadosGlobais.getInstance().getListaSetores();
        if (setores != null) {
            String chave = normalizeCodigo(cod);
            for (SetorLocalizacao s : setores) {
                if (s != null && chave.equals(normalizeCodigo(s.codlocalizacao))
                        && s.setor != null && !s.setor.isEmpty()) {
                    return s.setor;
                }
            }
        }
        return cod;
    }

    private static String addLeadingZeros(String v, int width) {
        if (!v.matches("^\\d+$")) return v;
        try { return String.format("%0" + width + "d", Integer.parseInt(v)); }
        catch (Exception e) { return v; }
    }
    private static String normalizeCodigo(String raw) {
        String s = safe(raw).replace("\u00A0", "");
        if (s.matches("^\\d+$")) s = s.replaceFirst("^0+(?!$)", "");
        return s;
    }
    private static String safe(String v) { return v == null ? "" : v.trim(); }
}
