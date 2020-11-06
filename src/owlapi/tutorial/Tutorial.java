
package owlapi.tutorial;

import org.coode.owlapi.obo12.parser.OBO12DocumentFormat;
import org.json.JSONArray;
import org.json.JSONObject;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.formats.*;
import org.semanticweb.owlapi.model.*;

import org.semanticweb.owlapi.util.CachingBidirectionalShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Testes para conhecimento da OWL API e validacao de Axiomas
 *
 */
class Provider extends CachingBidirectionalShortFormProvider {

	private SimpleShortFormProvider provider = new SimpleShortFormProvider();

	@Override
	protected String generateShortForm(OWLEntity entity) {
		return provider.getShortForm(entity);
	}
}

public class Tutorial {
	
	public void inicio() {
		
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		File arq = new File("/Users/eduardofelipe/Workspace/AxiomValidator/res/pizza.owl");
		OWLOntology o;

		try {
			o = man.loadOntologyFromOntologyDocument(arq);
			System.out.println(o);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		System.out.println(man.getOntologies().size());
	}

	public void testeAxioma() {
		IRI iri = IRI.create("http://teste.qq.endereco");
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		try {
			OWLOntology o = man.createOntology(iri);
			OWLDataFactory df = o.getOWLOntologyManager().getOWLDataFactory();
			OWLClass pessoa = df.getOWLClass(iri + "#Pessoa");
			OWLDeclarationAxiom da = df.getOWLDeclarationAxiom(pessoa);
			o.addAxiom(da);

			OWLClass mulher = df.getOWLClass(iri + "#Mulher");
			OWLSubClassOfAxiom m_sub_p = df.getOWLSubClassOfAxiom(mulher, pessoa);
			o.addAxiom(m_sub_p);

			//salvar ontologia
			File arq = new File("/Users/eduardofelipe/Workspace/AxiomValidator/res/saida.owl");
			OWLDocumentFormat formato = man.getOntologyFormat(o);
			try {
				man.saveOntology(o, formato, IRI.create(arq));
			} catch (OWLOntologyStorageException e) {
				e.printStackTrace();
			}

			//salvar em formato Turtle
			File arqTurtle = new File("/Users/eduardofelipe/Workspace/AxiomValidator/res/saidaTurtle.ttl");
			OWLDocumentFormat formatoTurtle = new TurtleDocumentFormat();
			try {
				man.saveOntology(o, formatoTurtle, IRI.create(arqTurtle));

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				man.saveOntology(o, formatoTurtle, baos);

				System.out.println("saida do stream");
				System.out.println(baos.toString());

			} catch (OWLOntologyStorageException e) {
				e.printStackTrace();
			}

			//saida console
			System.out.println(o);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * https://github.com/owlcs/owlapi/issues/507
	 * https://stackoverflow.com/questions/21005908/convert-string-in-manchester-syntax-to-owlaxiom-object-using-owlapi-3-in-java
	 * https://github.com/owlcs/owlapi/wiki/DL-Queries-with-a-real-reasoner
	 */
	public void testeValidacao() {
		OWLDataFactory df = OWLManager.getOWLDataFactory();

		Provider shortFormProvider = new Provider();
		OWLEntityChecker entityChecker = new ShortFormEntityChecker(shortFormProvider);
		shortFormProvider.add(df.getOWLClass(IRI.create("http://example.org/Arm")));
		shortFormProvider.add(df.getOWLClass(IRI.create("http://example.org/Finger")));
		shortFormProvider.add(df.getOWLObjectProperty(IRI.create("http://example.org/hasPart")));

		String input = "Arma subClassOf (hasPart some Finger)";
		ManchesterOWLSyntaxParser parser = OWLManager.createManchesterParser();
		parser.setOWLEntityChecker(entityChecker);
		parser.setStringToParse(input);
		try {
			OWLAxiom axiom = parser.parseAxiom();
			System.out.println("CheckManchesterSyntax.main() " + axiom.toString());
		}
		catch (Exception e) {
			System.out.println("Informe o seguinte erro ao suporte: " + e.getMessage());
		}
	}

	public String formataOWL(String ontologia) {
		JSONObject owl = new JSONObject(ontologia);

		String tipoFormato = owl.getString("formato");

		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLDocumentFormat formato = getFormatoSaidaOntologia(tipoFormato);
		try {
			OWLOntology owlOntology = geraOWLdeString(ontologia);
			if (owlOntology != null) {
//				if (tipoFormato.equals("OWL")) {
//					formato = new OWLXMLDocumentFormat();//man.getOntologyFormat(owlOntology);
//				} else {
//					formato = new TurtleDocumentFormat();
//				}
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				man.saveOntology(owlOntology, formato, baos);
				return baos.toString();
			} else {
				return "Erro: null";
			}
		} catch (Exception e) {
			return "Ocorreu o seguinte erro: " + e.getMessage();
		}
	}

	public OWLOntology geraOWLdeString(String ontologia) throws OWLOntologyCreationException {
		JSONObject owl = new JSONObject(ontologia);

		String id = owl.getString("id");

		IRI iri = IRI.create(id);
		OWLOntologyManager owlManager = OWLManager.createOWLOntologyManager();
		OWLOntology owlOntology;

		owlOntology = owlManager.createOntology(iri);
		OWLDataFactory dataFactory = owlOntology.getOWLOntologyManager().getOWLDataFactory();

		Provider shortFormProvider = new Provider();
		OWLEntityChecker entityChecker = new ShortFormEntityChecker(shortFormProvider);
		/**
		 * Trabalha se as classes
		 */
		JSONArray classes = new JSONArray();
		classes = owl.getJSONArray("classes");

		for (int i = 0; i < classes.length(); i++) {
			shortFormProvider.add(dataFactory.getOWLClass(iri.toString() + "#" + classes.get(i)));
		}

		/**
		 * Trabalha se os axiomas declarativos
		 */
		JSONArray axiomas = new JSONArray();
		axiomas = owl.getJSONArray("axiomas");
		ManchesterOWLSyntaxParser parser = OWLManager.createManchesterParser();
		parser.setOWLEntityChecker(entityChecker);

		try {
			for (int i = 0; i < axiomas.length(); i++) {
				parser.setStringToParse(axiomas.getString(i));
				owlOntology.addAxiom(parser.parseAxiom());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return owlOntology;
	}

	public String validaOWL(String ontologia) {
		OWLDataFactory df = OWLManager.getOWLDataFactory();

		Provider shortFormProvider = new Provider();
		OWLEntityChecker entityChecker = new ShortFormEntityChecker(shortFormProvider);

		//JSONObject owl2 = this.preparaElementos();
		//System.out.println(owl2);
		JSONObject owl = new JSONObject(ontologia);

		/**
		 * Trabalha se as classes
		 */
		JSONArray classes = new JSONArray();
		classes = owl.getJSONArray("classes");

		for (int i = 0; i < classes.length(); i++) {
			shortFormProvider.add(df.getOWLClass(IRI.create("https://onto4alleditor.com/pt/idDoProjeto/" + classes.get(i))));
		}

		/**
		 * Trabalha se os axiomas
		 */
		JSONArray axiomas = new JSONArray();
		axiomas = owl.getJSONArray("axiomas");
		ManchesterOWLSyntaxParser parser = OWLManager.createManchesterParser();
		parser.setOWLEntityChecker(entityChecker);

		for (int i = 0; i < axiomas.length(); i++) {
			parser.setStringToParse(axiomas.getString(i));
		}

		try {
			OWLAxiom axiom = parser.parseAxiom();
			return "Axioma válido " + axiom.toString();
		} catch (Exception e) {
			return "Axioma inválido: " + e.toString();
		}
	}

	public OWLDocumentFormat getFormatoSaidaOntologia(String formato) {
		OWLDocumentFormat documentFormat = null;

		if (formato.equals("OWL")) {
			documentFormat = new OWLXMLDocumentFormat();
		} else if (formato.equals("TURTLE")) {
			documentFormat = new TurtleDocumentFormat();
		} else if (formato.equals("SINTAXEDL")) {
			documentFormat = new DLSyntaxDocumentFormat();
		} else if (formato.equals("SINTAXEDLHTML")) {
			documentFormat = new DLSyntaxHTMLDocumentFormat();
		} else if (formato.equals("SINTAXEFUNCIONAL")) {
			documentFormat = new FunctionalSyntaxDocumentFormat();
		} else if (formato.equals("KRSS")) {
			documentFormat = new KRSS2DocumentFormat();
		} else if (formato.equals("DOCUMENTOLATEX")) {
			documentFormat = new LatexDocumentFormat();
		} else if (formato.equals("N3")) {
			documentFormat = new N3DocumentFormat();
		} else if (formato.equals("SINTAXEMANCHERTER")) {
			documentFormat = new ManchesterSyntaxDocumentFormat();
		} else if (formato.equals("NQUAD")) {
			documentFormat = new NQuadsDocumentFormat();
		} else if (formato.equals("NTRIPLA")) {
			documentFormat = new NTriplesDocumentFormat();
		} else if (formato.equals("OBO")) {
			documentFormat = new OBODocumentFormat();
		} else if (formato.equals("RDFJSON")) {
			documentFormat = new RDFJsonDocumentFormat();
		} else if (formato.equals("RDFJSONLD")) {
			documentFormat = new RDFJsonLDDocumentFormat();
		} else if (formato.equals("RDFXML")) {
			documentFormat = new RDFXMLDocumentFormat();
		} else if (formato.equals("RIOTURTLE")) {
			documentFormat = new RioTurtleDocumentFormat();
		} else if (formato.equals("RIORDFXML")) {
			documentFormat = new RioRDFXMLDocumentFormat();
		} else if (formato.equals("TRIG")) {
			documentFormat = new TrigDocumentFormat();
		} else if (formato.equals("TRIX")) {
			documentFormat = new TrixDocumentFormat();
		}
		return documentFormat;
	}
}

