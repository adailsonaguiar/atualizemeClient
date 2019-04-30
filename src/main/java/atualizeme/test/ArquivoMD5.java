package atualizeme.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import atualizeme.model.ArquivoTxt;

public class ArquivoMD5 {

	private static String nome;
	private String pastaAplicacao;

	public static void main(String[] args) throws NoSuchAlgorithmException, IOException {

//		List<ArquivoTxt> listaServidor = readFile(
//				System.getProperty("user.home") + File.separator + "oias" + File.separator + "MD5.txt");
//		List<ArquivoTxt> listacliente = readFile(
//				System.getProperty("user.home") + File.separator + "Downloads" + File.separator + "oias" + File.separator + "MD5.txt");
//
//		System.out.println(comparaListas(listaServidor, listacliente));

	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		ArquivoMD5.nome = nome;
	}

	public String getPastaAplicacao() {
		return pastaAplicacao;
	}

	public void setPastaAplicacao(String pastaAplicacao) {
		this.pastaAplicacao = pastaAplicacao;
	}

	public void arquivomd5(String caminhoMD5, String nomeMD5)
			throws NoSuchAlgorithmException, FileNotFoundException, IOException {
		File md5 = new File(caminhoMD5 + nomeMD5);
		if (!md5.exists()) {
			List<ArquivoTxt> lista = listaCaminhos(new File(pastaAplicacao));
			for (int i = 0; i < lista.size(); i++) {
				writeFile(caminhoMD5, nomeMD5, lista.get(i).getCaminhoLiteral(), lista.get(i).getCaminhoPasta(),
						geraHash(new File(lista.get(i).getCaminhoLiteral())));
			}
		} else if (md5.delete()) {
			List<ArquivoTxt> lista = listaCaminhos(new File(pastaAplicacao));
			for (int i = 0; i < lista.size(); i++) {
				writeFile(caminhoMD5, nomeMD5, lista.get(i).getCaminhoLiteral(), lista.get(i).getCaminhoPasta(),
						geraHash(new File(lista.get(i).getCaminhoLiteral())));
			}

		}
	}

	public void writeFile(String caminhoMD5, String nomeMD5, String caminhoLiteral, String caminhoPasta, String hashDir)
			throws IOException {
		FileWriter write = new FileWriter(caminhoMD5 + nomeMD5, true);
		write.write(caminhoLiteral);
		write.write(";");
		write.write(caminhoPasta);
		write.write(";");
		write.write(hashDir);
		write.write("\n");
		write.flush();
		write.close();
	}

	public List<ArquivoTxt> readFile(String pathFile) {

		List<ArquivoTxt> content = new ArrayList<ArquivoTxt>();
		try {
			FileReader arq = new FileReader(pathFile);
			BufferedReader buffer = new BufferedReader(arq);
			String linha = null;

			while ((linha = buffer.readLine()) != null) {

				String[] dados = linha.split(";");
				// String caminhoPasta, String caminhoLiteral, String hashFile, File file
				content.add(new ArquivoTxt(dados[1].toString(), dados[0].toString(), dados[2].toString(), null));
			}
			buffer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content;
	}

	public List<ArquivoTxt> listaCaminhos(File dir) {
		// File dir ---> Pasta onde se quer listar os caminhos
		List<ArquivoTxt> fileTree = new ArrayList<ArquivoTxt>();
		if (dir == null || dir.listFiles() == null) {
			List<ArquivoTxt> fileTree2 = (List<ArquivoTxt>) fileTree;
			return fileTree2;
		}
		for (File entry : dir.listFiles()) {
			if (entry.isFile()) {
//				fileTree.add(entry);
//				String[] dados = entry.getAbsolutePath().split("oias" + File.separator);
				String[] dados = entry.getAbsolutePath().split("oias" + File.separator + File.separator);
				fileTree.add(new ArquivoTxt(dados[1], entry.getAbsolutePath(), "", entry));
			} else
				fileTree.addAll(listaCaminhos(entry));
		}
		return (List<ArquivoTxt>) fileTree;
	}

	public String geraHash(File f) throws NoSuchAlgorithmException, FileNotFoundException {
		MessageDigest digest = MessageDigest.getInstance("MD5");
		InputStream is = new FileInputStream(f);
		byte[] buffer = new byte[8192];
		int read = 0;
		String output = null;
		try {
			while ((read = is.read(buffer)) > 0) {
				digest.update(buffer, 0, read);
			}
			byte[] md5sum = digest.digest();
			BigInteger bigInt = new BigInteger(1, md5sum);
			output = bigInt.toString(16);
		} catch (IOException e) {
			throw new RuntimeException("Não foi possivel processar o arquivo.", e);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				throw new RuntimeException("Não foi possivel fechar o arquivo", e);
			}
		}
		return output;
	}

	public List<ArquivoTxt> arquivosExcluir(List<ArquivoTxt> listaServidor, List<ArquivoTxt> listacliente) {
		List<ArquivoTxt> arqExlusao = new ArrayList<ArquivoTxt>();
		for (int i = 0; i < listacliente.size(); i++) {
			if (!listaServidor.contains(listacliente.get(i))) {
				arqExlusao.add(new ArquivoTxt(listacliente.get(i).getCaminhoPasta(), "", "", null));
			}
		}
		return arqExlusao;
	}

	public List<ArquivoTxt> arquivosAdiconar(List<ArquivoTxt> listaServidor, List<ArquivoTxt> listacliente) {
		List<ArquivoTxt> arqAdicionar = new ArrayList<ArquivoTxt>();
		for (int i = 0; i < listacliente.size(); i++) {
			if (!listacliente.contains(listaServidor.get(i))) {
				arqAdicionar.add(new ArquivoTxt(listaServidor.get(i).getCaminhoPasta(), "", "", null));
			}
		}
		return arqAdicionar;
	}

	public List<ArquivoTxt> comparaListas(List<ArquivoTxt> listaServidor, List<ArquivoTxt> listacliente) {
		List<ArquivoTxt> arqEnvio = new ArrayList<ArquivoTxt>();
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
