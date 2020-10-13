/**
 * 
 */
package owlapi.tutorial;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.BidirectionalShortFormProvider;
import org.semanticweb.owlapi.util.CachingBidirectionalShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser;

import java.io.File;

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

		String input = "Arm subClassOf (hasPart some Finger)";
		ManchesterOWLSyntaxParser parser = OWLManager.createManchesterParser();
		parser.setOWLEntityChecker(entityChecker);
		parser.setStringToParse(input);
		OWLAxiom axiom = parser.parseAxiom();
		System.out.println("CheckManchesterSyntax.main() " + axiom);
	}
}
