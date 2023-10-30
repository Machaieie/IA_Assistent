import java.io.*;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class AgenteLogico {

    public static void main(String[] args) {
        Map<String, String> conhecimento = carregarConhecimentoDoArquivo("conhecimento.txt");

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Você: ");
            String pergunta = scanner.nextLine();

            if (pergunta.equalsIgnoreCase("sair")) {
                System.out.println("Obrigado por usar o Agente Lógico. Até logo!");
                break;
            }

            String resposta = buscarResposta(conhecimento, pergunta);

            if (resposta != null) {
                System.out.println("Bot: " + resposta);
            } else if (contemPalavraChave(pergunta, "hora")) {
                // Se a pergunta contém a palavra "hora", obtenha a hora atual.
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                String horaAtual = sdf.format(new Date());
                System.out.println("Bot: Agora são " + horaAtual);
            } else if (contemPalavraChave(pergunta, "dia")) {
                // Se a pergunta contém a palavra "dia", obtenha o dia atual.
                Calendar cal = Calendar.getInstance();
                int diaAtual = cal.get(Calendar.DAY_OF_MONTH);
                System.out.println("Bot: Hoje é dia " + diaAtual);
            } else if (contemPalavraChave(pergunta, "mês")) {
                // Se a pergunta contém a palavra "mês", obtenha o mês atual.
                SimpleDateFormat sdf = new SimpleDateFormat("MM");
                String mesAtual = sdf.format(new Date());
                System.out.println("Bot: Estamos no mês " + mesAtual);
            } else if (contemPalavraChave(pergunta, "ano")) {
                // Se a pergunta contém a palavra "ano", obtenha o ano atual.
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
                String anoAtual = sdf.format(new Date());
                System.out.println("Bot: Estamos no ano de " + anoAtual);
            } else {
                String respostaAproximada = encontrarRespostaMaisProxima(conhecimento, pergunta);

                if (respostaAproximada != null) {
                    System.out.println("Bot: " + respostaAproximada);
                } else {
                    System.out.println("Bot: Desculpe, não sei a resposta para essa pergunta.");
                    System.out.print("Bot: Você sabe a resposta? Se sim, por favor, me diga: ");
                    String novaResposta = scanner.nextLine();
                    if (!novaResposta.isEmpty()) {
                        conhecimento.put(normalizarTexto(pergunta), novaResposta);
                        atualizarConhecimentoNoArquivo("conhecimento.txt", pergunta, novaResposta);
                        System.out.println("Bot: Obrigado por me ensinar! A resposta foi registrada.");
                    } else {
                        System.out.println("Bot: Ok, sem problemas.");
                    }
                }
            }
        }

        scanner.close();
    }

    public static Map<String, String> carregarConhecimentoDoArquivo(String nomeArquivo) {
        Map<String, String> conhecimento = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(nomeArquivo))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] partes = linha.split("=");
                if (partes.length == 2) {
                    conhecimento.put(normalizarTexto(partes[0]), partes[1]);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo de conhecimento não encontrado. O programa será encerrado.");
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return conhecimento;
    }

    public static String buscarResposta(Map<String, String> conhecimento, String pergunta) {
        pergunta = normalizarTexto(pergunta);
        return conhecimento.get(pergunta);
    }

    public static void atualizarConhecimentoNoArquivo(String nomeArquivo, String pergunta, String resposta) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(nomeArquivo, true))) {
            String linha = pergunta + "=" + resposta;
            writer.write(linha);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String normalizarTexto(String texto) {
        texto = Normalizer.normalize(texto, Normalizer.Form.NFD);
        texto = texto.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return texto.toLowerCase();
    }

    public static String encontrarRespostaMaisProxima(Map<String, String> conhecimento, String pergunta) {
        String perguntaNormalizada = normalizarTexto(pergunta);
        double maiorSimilaridade = 0.0;
        String respostaMaisSimilar = null;

        for (Map.Entry<String, String> entry : conhecimento.entrySet()) {
            String perguntaConhecimento = entry.getKey();
            double similaridade = calcularSimilaridade(perguntaNormalizada, perguntaConhecimento);
            if (similaridade > maiorSimilaridade) {
                maiorSimilaridade = similaridade;
                respostaMaisSimilar = entry.getValue();
            }
        }

        return respostaMaisSimilar;
    }

    public static double calcularSimilaridade(String pergunta, String perguntaConhecimento) {
        int maxLength = Math.max(pergunta.length(), perguntaConhecimento.length());
        int distance = calculateLevenshteinDistance(pergunta, perguntaConhecimento);
        double similarity = (double) (maxLength - distance) / maxLength;
        return similarity;
    }

    public static int calculateLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        return dp[s1.length()][s2.length()];
    }

    public static boolean contemPalavraChave(String texto, String palavraChave) {
        return normalizarTexto(texto).contains(normalizarTexto(palavraChave));
    }
}
