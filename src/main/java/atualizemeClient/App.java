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

	public static String URL_VERIFICACAO = "http://localhost:8080/atualizeme/api/update/verificar";
	public static String URL_DOWNLOAD = "http://localhost:8080/atualizeme/api/update/getArquivo";
	public static String CAMINHO = System.getProperty("user.home") + File.separator + "Downloads" + File.separator
			+ "oias" + File.separator;

	public static void main(String[] args) throws NoSuchAlgorithmException, FileNotFoundException, IOException {

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
		md5.arquivomd5(md5.getPastaAplicacao(), md5.getNome());

		List<ArquivoTxt> arquivosLocal = md5.readFile(md5.getPastaAplicacao() + md5.getNome());

		System.out.println("Verificando Atualizações");
		List<ArquivoTxt> arquivosServidor = verificaAtualizacao();
		List<ArquivoTxt> arquivosDownload = md5.comparaListas(arquivosServidor, arquivosLocal);

		for (int i = 0; i < arquivosDownload.size(); i++) {
			System.out.println("Atualizando Arquivos");
			baixaArquivos(arquivosDownload.get(i).getCaminhoPasta());
		}

		// Deletando arquivos
		List<ArquivoTxt> arquivosExlusao = md5.arquivosExcluir(arquivosServidor, arquivosLocal);
		for (int i = 0; i < arquivosExlusao.size(); i++) {
			deletarArquivos(arquivosExlusao.get(i).getCaminhoPasta());
		}

		List<ArquivoTxt> arquivosAdd = md5.arquivosAdiconar(arquivosServidor, arquivosLocal);
		for (int i = 0; i < arquivosAdd.size(); i++) {
			baixaArquivos(arquivosAdd.get(i).getCaminhoPasta());

		}

		System.out.println("Tudo pronto!");
	}

	public static List<ArquivoTxt> verificaAtualizacao() {
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

	public static void baixaArquivos(String caminhoPastaArquivo) throws IOException {
		Client client = ClientBuilder.newClient();
		String encodedString = Base64.getEncoder().encodeToString(caminhoPastaArquivo.getBytes());
		Response response = client.target(URL_DOWNLOAD).path(encodedString).request().get();

		String location = CAMINHO + caminhoPastaArquivo;

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

	public static void deletarArquivos(String caminhoPastaArquivo) {
		File f = new File(CAMINHO + caminhoPastaArquivo);
		f.delete();
	}
}
