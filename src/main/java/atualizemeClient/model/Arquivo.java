package atualizemeClient.model;

import java.io.File;

public class Arquivo {

	private String caminhoPasta;
	private String caminhoLiteral;
	private String hashFile;
	private File file;
	private String nome;

	public Arquivo(String caminhoPasta, String caminhoLiteral, String hashFile, File file, String nome) {
		this.caminhoPasta = caminhoPasta;
		this.caminhoLiteral = caminhoLiteral;
		this.hashFile = hashFile;
		this.file = file;
		this.nome = nome;
	}

	public String getCaminhoPasta() {
		return caminhoPasta;
	}

	public void setCaminhoPasta(String caminhoPasta) {
		this.caminhoPasta = caminhoPasta;
	}

	public String getCaminhoLiteral() {
		return caminhoLiteral;
	}

	public void setCaminhoLiteral(String caminhoLiteral) {
		this.caminhoLiteral = caminhoLiteral;
	}

	public String getHashFile() {
		return hashFile;
	}

	public void setHashFile(String hashFile) {
		this.hashFile = hashFile;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Override
	public boolean equals(Object arquivo) {
		String nomeArquivo = this.getCaminhoPasta() + this.getNome();
		String nomeArquivoCli = ((Arquivo) arquivo).getCaminhoPasta() + ((Arquivo) arquivo).getNome();
		if (nomeArquivo.equals(nomeArquivoCli)) {
			return true;
		}
		return false;
	}
}
