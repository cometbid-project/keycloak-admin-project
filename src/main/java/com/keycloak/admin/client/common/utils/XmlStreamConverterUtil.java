/**
 * 
 */
package com.keycloak.admin.client.common.utils;

import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * @author Gbenga
 *
 */
public class XmlStreamConverterUtil {

	public static <T> String fromModeltoXml(final T object, Class<? extends T> resultType) {

		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(resultType);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			StringWriter xmlWriter = new StringWriter();
			marshaller.marshal(object, xmlWriter);

			return xmlWriter.toString();
		} catch (JAXBException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static <T, E> String fromListModeltoXml(final E object, Class<? extends E> genericListType,
			Class<? extends T> resultType) {

		try {
			JAXBContext jc = JAXBContext.newInstance(genericListType, resultType);
			String xml = marshall(object, jc);

			return xml;
		} catch (JAXBException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static <T, E> E fromXmlToListModel(final String xml, Class<? extends E> genericListType,
			Class<? extends T> resultType) {

		try {
			JAXBContext jc = JAXBContext.newInstance(genericListType, resultType);
			E retr = Unmarshall(xml, jc);

			return retr;
		} catch (JAXBException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T fromXmlToModel(final String xml, Class<? extends T> resultType) {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(resultType);
			StringWriter xmlWriter = new StringWriter();

			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			StringReader xmlReader = new StringReader(xmlWriter.toString());

			return (T) unmarshaller.unmarshal(xmlReader);

		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static <O> String marshall(O o, JAXBContext jc) throws JAXBException {

		// Creating a Marshaller
		Marshaller jaxbMarshaller = jc.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		StringWriter result = new StringWriter();
		jaxbMarshaller.marshal(o, result);

		// Printing XML
		return result.toString();
	}

	@SuppressWarnings("unchecked")
	public static <O> O Unmarshall(final String xml, JAXBContext jc) throws JAXBException {
		// Creating an Unmarshaller
		Unmarshaller jaxbUnmarshaller = jc.createUnmarshaller();
		StringReader sr = new StringReader(xml);

		O retr = (O) jaxbUnmarshaller.unmarshal(sr);

		return retr;
	}
}
