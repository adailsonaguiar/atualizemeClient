package atualizemeClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import atualizemeClient.model.Arquivo;

public class ArquivosAtualizacao {

	public static void main(String[] args) throws NoSuchAlgorithmException, IOException {

//		List<ArquivoTxt> listaServidor = readFile(
//				System.getProperty("user.home") + File.separator + "oias" + File.separator + "MD5.txt");
//		List<ArquivoTxt> listacliente = readFile(
//				System.getProperty("user.home") + File.separator + "Downloads" + File.separator + "oias" + File.separator + "MD5.txt");
//
//		System.out.println(comparaListas(listaServidor, listacliente));

		String location = "/home/adailson/Downloads/oias/drawable-xxhdpi/icon.png";

		File f = new File(location);
		System.out.println(f.getParentFile());
// FileOutputStream out = new FileOutputStream(location);

	}

	public List<Arquivo> readFile(String pathFile) throws IOException {

		List<Arquivo> content = new ArrayList<Arquivo>();
		FileReader arq = new FileReader(pathFile);
		BufferedReader buffer = new BufferedReader(arq);
		String linha = null;

		while ((linha = buffer.readLine()) != null) {

			String[] dados = linha.split(";");
			// String caminhoPasta, String caminhoLiteral, String hashFile, File file
			content.add(new Arquivo(dados[1].toString(), dados[0].toString(), dados[2].toString(), null,
					dados[3].toString()));
		}
		buffer.close();
		return content;
	}

	public List<Arquivo> excluirArquivos(List<Arquivo> listaServidor, List<Arquivo> listacliente) {
		List<Arquivo> arqExlusao = new ArrayList<Arquivo>();
		for (int i = 0; i < listacliente.size(); i++) {
			if (!listaServidor.contains(listacliente.get(i))) {
				arqExlusao.add(new Arquivo(listacliente.get(i).getCaminhoPasta(), "", "", null, ""));
			}
		}
		return arqExlusao;
	}

	public List<Arquivo> adiconarArquivos(List<Arquivo> listaServidor, List<Arquivo> listacliente) {
		List<Arquivo> arqAdicionar = new ArrayList<Arquivo>();
		for (int i = 0; i < listaServidor.size(); i++) {
			if (!listacliente.contains(listaServidor.get(i))) {
				arqAdicionar.add(new Arquivo(listaServidor.get(i).getCaminhoPasta(), "", "", null, ""));
			}
		}
		return arqAdicionar;
	}

	public List<Arquivo> comparaListas(List<Arquivo> listaServidor, List<Arquivo> listacliente) {
		List<Arquivo> arqEnvio = new ArrayList<Arquivo>();
		for (int i = 0; i < listacliente.size(); i++) {
			if (listaServidor.contains(listacliente.get(i))) {
				for (int j = 0; j < listaServidor.size(); j++) {
					if (listaServidor.get(j).getCaminhoPasta().equals(listacliente.get(i).getCaminhoPasta())) {
						if (!listaServidor.get(j).getHashFile().equals(listacliente.get(i).getHashFile())) {
							arqEnvio.add(listaServidor.get(j));
						}
					}
				}
			}
		}
		return arqEnvio;
	}

}
