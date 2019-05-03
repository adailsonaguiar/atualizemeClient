package atualizemeClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import atualizemeClient.model.Arquivo;

public class App {

	public static String URL_ARQUIVO_VERIFICACAO = "http://localhost:8080/atualizeme/api/update/arquivoverificacao";
	public static String URL_VERIFICACAO = "http://localhost:8080/atualizeme/api/update/verificaratualizacao";
	public static String URL_DOWNLOAD = "http://localhost:8080/atualizeme/api/update/getarquivo";
	public static String CAMINHO = System.getProperty("user.home") + File.separator + "Downloads" + File.separator
			+ "oias" + File.separator;
	public static List<Arquivo> arquivosLocal;
	public static List<Arquivo> arquivosServidor;

	public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
		ArquivosAtualizacao arquivosAtualizacao = new ArquivosAtualizacao();
		arquivosServidor = verificarAtualizacao();

		try {
			arquivosLocal = arquivosAtualizacao.readFile(CAMINHO + "MD5.txt");

			baixarArquivosAlteradosServidor(arquivosAtualizacao, arquivosServidor, arquivosLocal);

			excluirArquivosNaoContidosNoServidor(arquivosAtualizacao, arquivosServidor, arquivosLocal);

			baixarArquivosNovos(arquivosAtualizacao, arquivosServidor, arquivosLocal);

			baixarArquivoAtualizacao();

			renomearArquivosTemp(arquivosAtualizacao);

		} catch (IOException e) {
			baixarTodos(arquivosAtualizacao, arquivosServidor);
			baixarArquivoAtualizacao();
			renomearArquivosTemp(arquivosAtualizacao);
		}

	}

	public static void baixarTodos(ArquivosAtualizacao arquivosAtualizacao, List<Arquivo> arquivosServidor) {
		try {
			for (int i = 0; i < arquivosServidor.size(); i++) {
				baixarArquivo(arquivosAtualizacao, arquivosServidor.get(i).getCaminhoPasta(),
						arquivosServidor.get(i).getNome());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static boolean baixarArquivoAtualizacao() {
		try {
			Client client = ClientBuilder.newClient();
			Response response = client.target(URL_ARQUIVO_VERIFICACAO).request().get();

			String location = CAMINHO + "MD5.txt";

			FileOutputStream out = new FileOutputStream(location);
			InputStream is = (InputStream) response.getEntity();

			int len = 0;
			byte[] buffer = new byte[4096];
			while ((len = is.read(buffer)) != -1) {
				out.write(buffer, 0, len);
			}
			out.flush();
			out.close();
			is.close();
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	public static List<Arquivo> verificarAtualizacao() {
		List<Arquivo> arquivoServidor;

		Client client = ClientBuilder.newClient();
		Response response = client.target(URL_VERIFICACAO).request().get();

		byte[] decodedBytes = Base64.getDecoder().decode(response.getHeaderString("listaServidor"));

		String decodedString = new String(decodedBytes);

		Type listType = new TypeToken<ArrayList<Arquivo>>() {
		}.getType();

		arquivoServidor = new Gson().fromJson(decodedString, listType);
		return arquivoServidor;
	}

	public static void baixarArquivo(ArquivosAtualizacao atualizacao, String caminhoPastaArquivo, String nome)
			throws IOException {
		Client client = ClientBuilder.newClient();
		String caminhoEncode = Base64.getEncoder().encodeToString(caminhoPastaArquivo.getBytes());
		String nomeEncode = Base64.getEncoder().encodeToString(nome.getBytes());
		Response response = client.target(URL_DOWNLOAD).path(caminhoEncode).path(nomeEncode).request().get();

		String location = CAMINHO + caminhoPastaArquivo + "_temp_" + nome;
		criarPastaPai(location);
		FileOutputStream out = new FileOutputStream(location);
		InputStream is = (InputStream) response.getEntity();

		int len = 0;
		byte[] buffer = new byte[4096];
		while ((len = is.read(buffer)) != -1) {
			out.write(buffer, 0, len);
		}
		atualizacao.adicionaListaTemporarios(caminhoPastaArquivo, "_temp_" + nome);

		out.flush();
		out.close();
		is.close();
	}

	public static boolean criarPastaPai(String caminhoArquivo) {
		File f = new File(caminhoArquivo);
		String pathParent = f.getParent() + File.separator;
		if (pathParent.equals(CAMINHO)) {
			return true;
		}
		File newFolder = new File(pathParent);
		return newFolder.mkdirs();
	}

	public static void baixarArquivosNovos(ArquivosAtualizacao arquivosAtualizacao, List<Arquivo> arquivosServidor,
			List<Arquivo> arquivosLocal) {
		List<Arquivo> arquivosAdd = arquivosAtualizacao.adiconarArquivos(arquivosServidor, arquivosLocal);
		for (int i = 0; i < arquivosAdd.size(); i++) {
			try {
				baixarArquivo(arquivosAtualizacao, arquivosAdd.get(i).getCaminhoPasta(), arquivosAdd.get(i).getNome());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(arquivosAdd.get(i).getCaminhoPasta());
		}
	}

	public static void excluirArquivosNaoContidosNoServidor(ArquivosAtualizacao arquivosAtualizacao,
			List<Arquivo> arquivosServidor, List<Arquivo> arquivosLocal) {
		List<Arquivo> arquivosExlusao = arquivosAtualizacao.excluirArquivos(arquivosServidor, arquivosLocal);

		for (int i = 0; i < arquivosExlusao.size(); i++) {
			if (!arquivosExlusao.get(i).getCaminhoPasta().equals("MD5.txt")) {
				deletarArquivo(arquivosExlusao.get(i).getCaminhoPasta(), arquivosExlusao.get(i).getNome());
			}
		}
	}

	public static void baixarArquivosAlteradosServidor(ArquivosAtualizacao arquivosAtualizacao,
			List<Arquivo> arquivosServidor, List<Arquivo> arquivosLocal) {
		try {
			List<Arquivo> arquivosDownload = arquivosAtualizacao.comparaListas(arquivosServidor, arquivosLocal);
			for (int i = 0; i < arquivosDownload.size(); i++) {
				System.out.println("Atualizando Arquivos");

				baixarArquivo(arquivosAtualizacao, arquivosDownload.get(i).getCaminhoPasta(),
						arquivosDownload.get(i).getNome());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void deletarArquivo(String caminhoPastaArquivo, String nome) {
		File f = new File(CAMINHO + caminhoPastaArquivo + nome);
		f.delete();
	}

	public static void renomearArquivosTemp(ArquivosAtualizacao arquivosAtualizacao) {
		for (int i = 0; i < arquivosAtualizacao.getListaTemporaria().size(); i++) {
			String nomeTemporario = arquivosAtualizacao.getListaTemporaria().get(i).getNome();
			String[] nomeNovo = nomeTemporario.split("_temp_");

			File arquivoTemporario = new File(
					CAMINHO + arquivosAtualizacao.getListaTemporaria().get(i).getCaminhoPasta()
							+ arquivosAtualizacao.getListaTemporaria().get(i).getNome());
			File arquivoNovo = new File(
					CAMINHO + arquivosAtualizacao.getListaTemporaria().get(i).getCaminhoPasta() + nomeNovo[1]);
			if (arquivoNovo.exists()) {
				arquivoNovo.delete();
			}
			arquivoTemporario.renameTo(arquivoNovo);
		}
	}
}
