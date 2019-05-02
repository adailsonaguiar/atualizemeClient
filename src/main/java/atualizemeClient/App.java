package atualizemeClient;

import java.io.File;
import java.io.FileNotFoundException;
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

import atualizeme.model.ArquivoTxt;
import atualizeme.test.ArquivoMD5;

public class App {

	public static String URL_ARQUIVO_VERIFICACAO = "http://localhost:8080/atualizeme/api/update/arquivoverificacao";
	public static String URL_VERIFICACAO = "http://localhost:8080/atualizeme/api/update/verificaratualizacao";
	public static String URL_DOWNLOAD = "http://localhost:8080/atualizeme/api/update/getarquivo";
	public static String CAMINHO = System.getProperty("user.home") + File.separator + "Downloads" + File.separator
			+ "oias" + File.separator;
	public static List<ArquivoTxt> arquivosLocal;
	public static List<ArquivoTxt> arquivosServidor;

	public static void main(String[] args) throws NoSuchAlgorithmException, IOException {

//		boolean end = false;
//		while (!end) {
//			ArquivoMD5 md5 = new ArquivoMD5();
//			md5.setNome("MD5.txt");
//			md5.setPastaAplicacao(caminho);
//			md5.arquivomd5(md5.getPastaAplicacao(), md5.getNome());
//
//			List<ArquivoTxt> listacliente = md5.readFile(md5.getPastaAplicacao() + md5.getNome());
//
//			String json = new Gson().toJson(listacliente);
//			String encodedString = Base64.getEncoder().encodeToString(json.getBytes());
//
//			Client client = ClientBuilder.newClient();
//			Response response = client.target("http://localhost:8080/atualizeme/api/update/get").path(encodedString)
//					.request().get();
//
//			if (response.getStatus() == 200) {
//				end = true;
//				System.out.println("Tudo atualizado!");
//			} else {
//
//				String location = System.getProperty("user.home") + File.separator + "Downloads" + File.separator
//						+ "oias" + File.separator + response.getHeaderString("nomeArquivo");
//				System.out.println(location);
//
//				if (response.getHeaderString("listaExclusao") != null) {
//					byte[] decodedBytes = Base64.getDecoder().decode(response.getHeaderString("listaExclusao"));
//					String decodedString = new String(decodedBytes);
//					Type listType = new TypeToken<ArrayList<ArquivoTxt>>() {
//					}.getType();
//					List<ArquivoTxt> listaExclusao = new Gson().fromJson(decodedString, listType);
//
//					if (listaExclusao.size() > 0) {
//						for (int i = 0; i < listaExclusao.size(); i++) {
//							File f = new File(caminho + listaExclusao.get(i).getCaminhoPasta());
//							f.delete();
//						}
//					}
//				}
//
//				FileOutputStream out = new FileOutputStream(location);
//				InputStream is = (InputStream) response.getEntity();
//
//				int len = 0;
//				byte[] buffer = new byte[4096];
//				while ((len = is.read(buffer)) != -1) {
//					out.write(buffer, 0, len);
//				}
//
//				out.flush();
//				out.close();
//				is.close();
//			}
//		}

		ArquivoMD5 md5 = new ArquivoMD5();
		md5.setNome("MD5.txt");
		md5.setPastaAplicacao(CAMINHO);

		try {
			arquivosLocal = md5.readFile(md5.getPastaAplicacao() + md5.getNome());
			arquivosLocal = md5.readFile(md5.getPastaAplicacao() + md5.getNome());
			arquivosServidor = verificarAtualizacao();

			baixarArquivosAlteradosServidor(md5, arquivosServidor, arquivosLocal);

			excluirArquivosNaoContidosNoServidor(md5, arquivosServidor, arquivosLocal);

			baixarArquivosNovos(md5, arquivosServidor, arquivosLocal);
			baixarArquivoAtualizacao();
		} catch (IOException e) {
			baixarTodos(md5);
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

	public static void baixarTodos(ArquivoMD5 md5) {
		if (baixarArquivoAtualizacao()) {
			try {
				arquivosLocal = md5.readFile(md5.getPastaAplicacao() + md5.getNome());
				for (int i = 0; i < arquivosLocal.size(); i++) {
					baixarArquivo(arquivosLocal.get(i).getCaminhoPasta());
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static List<ArquivoTxt> verificarAtualizacao() {
		List<ArquivoTxt> arquivoServidor;

		Client client = ClientBuilder.newClient();
		Response response = client.target(URL_VERIFICACAO).request().get();

		byte[] decodedBytes = Base64.getDecoder().decode(response.getHeaderString("listaServidor"));

		String decodedString = new String(decodedBytes);

		Type listType = new TypeToken<ArrayList<ArquivoTxt>>() {
		}.getType();

		arquivoServidor = new Gson().fromJson(decodedString, listType);

		return arquivoServidor;
	}

	public static void baixarArquivo(String caminhoPastaArquivo) throws IOException {
		Client client = ClientBuilder.newClient();
		String encodedString = Base64.getEncoder().encodeToString(caminhoPastaArquivo.getBytes());
		Response response = client.target(URL_DOWNLOAD).path(encodedString).request().get();

		String location = CAMINHO + caminhoPastaArquivo;
		parentPath(location);
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

	public static boolean parentPath(String caminhoArquivo) {
		File f = new File(caminhoArquivo);
		String pathParent = f.getParent() + File.separator;
		if (pathParent.equals(CAMINHO)) {
			return true;
		}
		File newFolder = new File(pathParent);
		return newFolder.mkdir();
	}

	public static void baixarArquivosNovos(ArquivoMD5 md5, List<ArquivoTxt> arquivosServidor,
			List<ArquivoTxt> arquivosLocal) {
		List<ArquivoTxt> arquivosAdd = md5.adiconarArquivos(arquivosServidor, arquivosLocal);
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

	public static void excluirArquivosNaoContidosNoServidor(ArquivoMD5 md5, List<ArquivoTxt> arquivosServidor,
			List<ArquivoTxt> arquivosLocal) {
		File f = new File(CAMINHO);
		List<ArquivoTxt> arquivosExlusao = md5.excluirArquivos(arquivosServidor, arquivosLocal);

		for (int i = 0; i < arquivosExlusao.size(); i++) {
			if (!arquivosExlusao.get(i).getCaminhoPasta().equals("MD5.txt")) {
				deletarArquivo(arquivosExlusao.get(i).getCaminhoPasta());
			}
		}
	}

	public static void baixarArquivosAlteradosServidor(ArquivoMD5 md5, List<ArquivoTxt> arquivosServidor,
			List<ArquivoTxt> arquivosLocal) {
		try {
			List<ArquivoTxt> arquivosDownload = md5.comparaListas(arquivosServidor, arquivosLocal);
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
