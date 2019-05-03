package atualizemeClient;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import atualizemeClient.model.Arquivo;

public class ArquivosAtualizacao {

	private List<Arquivo> content = new ArrayList<Arquivo>();
	private List<Arquivo> arquivosTemporarios = new ArrayList<Arquivo>();
	private List<Arquivo> arqEnvio = new ArrayList<Arquivo>();
	private List<Arquivo> arqAdicionar = new ArrayList<Arquivo>();
	private List<Arquivo> arqExlusao = new ArrayList<Arquivo>();

	public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
	}

	public List<Arquivo> readFile(String pathFile) throws IOException {
		FileReader arq = new FileReader(pathFile);
		BufferedReader buffer = new BufferedReader(arq);
		String linha = null;

		while ((linha = buffer.readLine()) != null) {

			String[] dados = linha.split(";");
			content.add(new Arquivo(dados[1].toString(), dados[0].toString(), dados[2].toString(), null,
					dados[3].toString()));
		}
		buffer.close();
		return content;
	}

	public List<Arquivo> excluirArquivos(List<Arquivo> listaServidor, List<Arquivo> listacliente) {
		for (int i = 0; i < listacliente.size(); i++) {
			if (!listaServidor.contains(listacliente.get(i))) {
				arqExlusao.add(new Arquivo(listacliente.get(i).getCaminhoPasta(), "", "", null,
						listacliente.get(i).getNome()));
			}
		}
		return arqExlusao;
	}

	public List<Arquivo> adiconarArquivos(List<Arquivo> listaServidor, List<Arquivo> listacliente) {
		for (int i = 0; i < listaServidor.size(); i++) {
			if (!listacliente.contains(listaServidor.get(i))) {
				arqAdicionar.add(new Arquivo(listaServidor.get(i).getCaminhoPasta(), "", "", null,
						listaServidor.get(i).getNome()));
			}
		}
		return arqAdicionar;
	}

	public List<Arquivo> comparaListas(List<Arquivo> listaServidor, List<Arquivo> listacliente) {
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

	public void adicionaListaTemporarios(String caminhoPasta, String nome) {
		arquivosTemporarios.add(new Arquivo(caminhoPasta, "", "", null, nome));
	}

	public List<Arquivo> getListaTemporaria() {
		return arquivosTemporarios;
	}
}
