/**
 * Copyright 2012 Miroslav Šulc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.fordfrog.ruian2pgsql.convertors;

import com.fordfrog.ruian2pgsql.containers.Hlavicka;
import com.fordfrog.ruian2pgsql.utils.Namespaces;
import com.fordfrog.ruian2pgsql.utils.PreparedStatementEx;
import com.fordfrog.ruian2pgsql.utils.Utils;
import com.fordfrog.ruian2pgsql.utils.XMLUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Convertor of Hlavicka element.
 *
 * @author fordfrog
 */
public class HlavickaConvertor extends AbstractSaveConvertor<Hlavicka> {

    /**
     * Namespace of Hlavicka element and its sub-elements.
     */
    private static final String NAMESPACE = Namespaces.VYMENNY_FORMAT_TYPY;
    /**
     * SQL statement for testing whether item exists. We always return that the
     * item does not exist because we want all header information in the
     * database.
     */
    private static final String SQL_EXISTS =
            "SELECT 1 FROM hlavicka WHERE typ_zaznamu IS NULL";
    /**
     * SQL statement for insertion of item.
     */
    private static final String SQL_INSERT = "INSERT INTO hlavicka "
            + "(typ_zaznamu, typ_davky, typ_souboru, datum, transakce_od_id, "
            + "transakce_od_zapsano, transakce_do_id, transakce_do_zapsano, "
            + "predchozi_soubor, plny_soubor, metadata) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    /**
     * Creates new instance of HlavickaConvertor.
     *
     * @param con database connection
     *
     * @throws SQLException Thrown if problem occurred while communicating with
     *                      database.
     */
    public HlavickaConvertor(final Connection con) throws SQLException {
        super(Hlavicka.class, NAMESPACE, "Hlavicka", con, SQL_EXISTS,
                SQL_INSERT, null);
    }

    @Override
    protected void fill(final PreparedStatement pstm, final Hlavicka item,
            final boolean update) throws SQLException {
        final PreparedStatementEx pstmEx = new PreparedStatementEx(pstm);
        pstm.setString(1, item.getTypZaznamu());
        pstm.setString(2, item.getTypDavky());
        pstm.setString(3, item.getTypSouboru());
        pstmEx.setDate(4, item.getDatum());
        pstm.setInt(5, item.getTransakceOdId());
        pstmEx.setTimestamp(6, item.getTransakceOdZapsano());
        pstm.setInt(7, item.getTransakceDoId());
        pstmEx.setTimestamp(8, item.getTransakceDoZapsano());
        pstm.setString(9, item.getPredchoziSoubor());
        pstm.setString(10, item.getPlnySoubor());
        pstm.setString(11, item.getMetadata());
    }

    @Override
    protected void fillExists(final PreparedStatement pstm, final Hlavicka item)
            throws SQLException {
        // we always return empty resultset so we do not set any prepared
        // statement parameters
    }

    @Override
    protected void processElement(final XMLStreamReader reader,
            final Hlavicka item) throws XMLStreamException {
        switch (reader.getNamespaceURI()) {
            case NAMESPACE:
                switch (reader.getLocalName()) {
                    case "Datum":
                        item.setDatum(
                                Utils.parseTimestamp(reader.getElementText()));
                        break;
                    case "Metadata":
                        item.setMetadata(reader.getAttributeValue(
                                Namespaces.XLINK, "href"));
                        break;
                    case "PlnySoubor":
                        item.setPlnySoubor(reader.getElementText());
                        break;
                    case "PredchoziSoubor":
                        item.setPredchoziSoubor(reader.getElementText());
                        break;
                    case "TransakceOd":
                    case "TrasakceOd":
                        processTransakceOd(reader, item);
                        break;
                    case "TransakceDo":
                        processTransakceDo(reader, item);
                        break;
                    case "TypDavky":
                        item.setTypDavky(reader.getElementText());
                        break;
                    case "TypSouboru":
                        item.setTypSouboru(reader.getElementText());
                        break;
                    case "TypZaznamu":
                        item.setTypZaznamu(reader.getElementText());
                        break;
                    default:
                        XMLUtils.processUnsupported(reader);
                }

                break;
            default:
                XMLUtils.processUnsupported(reader);
        }
    }

    /**
     * Processes TransakceOd element.
     *
     * @param reader XML stream reader
     * @param item   item of the element
     *
     * @throws XMLStreamException Thrown if problem occurred while reading XML
     *                            stream.
     */
    private void processTransakceOd(final XMLStreamReader reader,
            final Hlavicka item) throws XMLStreamException {
        while (reader.hasNext()) {
            final int event = reader.next();

            switch (event) {
                case XMLStreamReader.START_ELEMENT:
                    processTransakceOdElement(reader, item);
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if (XMLUtils.isSameElement(NAMESPACE, "TransakceOd", reader)
                            || XMLUtils.isSameElement(
                            NAMESPACE, "TrasakceOd", reader)) {
                        return;
                    }
            }
        }
    }

    /**
     * Processes TransakceOd sub-elements.
     *
     * @param reader XML stream reader
     * @param header item of the element
     *
     * @throws XMLStreamException Thrown if problem occurred while reading XML
     *                            stream.
     */
    private void processTransakceOdElement(final XMLStreamReader reader,
            final Hlavicka header) throws XMLStreamException {
        switch (reader.getNamespaceURI()) {
            case Namespaces.COMMON_TYPY:
                switch (reader.getLocalName()) {
                    case "Id":
                        header.setTransakceOdId(
                                Integer.parseInt(reader.getElementText()));
                        break;
                    case "Zapsano":
                        header.setTransakceOdZapsano(
                                Utils.parseTimestamp(reader.getElementText()));
                        break;
                    default:
                        XMLUtils.processUnsupported(reader);
                }
                break;
            default:
                XMLUtils.processUnsupported(reader);
        }
    }

    /**
     * Processes TransakceDo element.
     *
     * @param reader XML stream reader
     * @param item   item of the element
     *
     * @throws XMLStreamException Thrown if problem occurred while reading XML
     *                            stream.
     */
    private void processTransakceDo(final XMLStreamReader reader,
            final Hlavicka header) throws XMLStreamException {
        while (reader.hasNext()) {
            final int event = reader.next();

            switch (event) {
                case XMLStreamReader.START_ELEMENT:
                    processTransakceDoElement(reader, header);
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if (XMLUtils.isSameElement(
                            NAMESPACE, "TransakceDo", reader)) {
                        return;
                    }
            }
        }
    }

    /**
     * Processes TransakceDo sub-elements.
     *
     * @param reader XML stream reader
     * @param header item of the element
     *
     * @throws XMLStreamException Thrown if problem occurred while reading XML
     *                            stream.
     */
    private void processTransakceDoElement(final XMLStreamReader reader,
            final Hlavicka header) throws XMLStreamException {
        switch (reader.getNamespaceURI()) {
            case Namespaces.COMMON_TYPY:
                switch (reader.getLocalName()) {
                    case "Id":
                        header.setTransakceDoId(
                                Integer.parseInt(reader.getElementText()));
                        break;
                    case "Zapsano":
                        header.setTransakceDoZapsano(
                                Utils.parseTimestamp(reader.getElementText()));
                        break;
                    default:
                        XMLUtils.processUnsupported(reader);
                }
                break;
            default:
                XMLUtils.processUnsupported(reader);
        }
    }
}
