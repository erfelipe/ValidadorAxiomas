import owlapi.tutorial.Tutorial;

public class main {

	public static void main(String[] args) {

		String onto = "{\n" +
				"    \"id\": \"https://onto4alleditor.com/pt/idDoProjeto/\",\n" +
				"    \"formato\": \"OWL\",\n" +
				"    \"classes\": [\"Pessoa\", \"Homem\", \"Mulher\"],\n" +
				"    \"axiomas\": [\"Homem subClassOf (Pessoa)\", \"Mulher subClassOf (Pessoa)\"],\n" +
				"    \"propriedades\": [\"hasPart\"]\n" +
				"} ";
		try {
			Tutorial tutorial = new Tutorial();
			//tutorial.inicio();
			//tutorial.testeValidacao();
			//tutorial.testeAxioma();
			System.out.println(tutorial.formataOWL(onto));
			tutorial.validaOWL(onto);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
	