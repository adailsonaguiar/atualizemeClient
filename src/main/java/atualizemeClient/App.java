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
		ArquivosAtualizacao md5 = new ArquivosAtualizacao();
		arquivosServidor = verificarAtualizacao();
		try {
			arquivosLocal = md5.readFile(CAMINHO + "MD5.txt");

			baixarArquivosAlteradosServidor(md5, arquivosServidor, arquivosLocal);

			excluirArquivosNaoContidosNoServidor(md5, arquivosServidor, arquivosLocal);

			baixarArquivosNovos(md5, arquivosServidor, arquivosLocal);
			baixarArquivoAtualizacao();
		} catch (IOException e) {
			baixarTodos(arquivosServidor);
			baixarArquivoAtualizacao();
		}
	}

	public static void baixarTodos(List<Arquivo> arquivosServidor) {
		try {
			for (int i = 0; i < arquivosServidor.size(); i++) {
				baixarArquivo(arquivosServidor.get(i).getCaminhoPasta());
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
		}
		return false;
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

	public static void baixarArquivo(String caminhoPastaArquivo) throws IOException {
		Client client = ClientBuilder.newClient();
		String encodedString = Base64.getEncoder().encodeToString(caminhoPastaArquivo.getBytes());
		Response response = client.target(URL_DOWNLOAD).path(encodedString).request().get();

		String location = CAMINHO + caminhoPastaArquivo;
		criarPastaPai(location);
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

	public static void baixarArquivosNovos(ArquivosAtualizacao md5, List<Arquivo> arquivosServidor,
			List<Arquivo> arquivosLocal) {
		List<Arquivo> arquivosAdd = md5.adiconarArquivos(arquivosServidor, arquivosLocal);
		for (int i = 0; i < arquivosAdd.size(); i++) {
			try {
				baixarArquivo(arquivosAdd.get(i).getCaminhoPasta());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(arquivosAdd.get(i).getCaminhoPasta());
		}
	}

	public static void excluirArquivosNaoContidosNoServidor(ArquivosAtualizacao md5, List<Arquivo> arquivosServidor,
			List<Arquivo> arquivosLocal) {
		List<Arquivo> arquivosExlusao = md5.excluirArquivos(arquivosServidor, arquivosLocal);

		for (int i = 0; i < arquivosExlusao.size(); i++) {
			if (!arquivosExlusao.get(i).getCaminhoPasta().equals("MD5.txt")) {
				deletarArquivo(arquivosExlusao.get(i).getCaminhoPasta());
			}
		}
	}

	public static void baixarArquivosAlteradosServidor(ArquivosAtualizacao md5, List<Arquivo> arquivosServidor,
			List<Arquivo> arquivosLocal) {
		try {
			List<Arquivo> arquivosDownload = md5.comparaListas(arquivosServidor, arquivosLocal);
			for (int i = 0; i < arquivosDownload.size(); i++) {
				System.out.println("Atualizando Arquivos");

				baixarArquivo(arquivosDownload.get(i).getCaminhoPasta());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void deletarArquivo(String caminhoPastaArquivo) {
		File f = new File(CAMINHO + caminhoPastaArquivo);
		f.delete();
	}
}
