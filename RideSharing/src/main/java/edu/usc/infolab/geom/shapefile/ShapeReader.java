package edu.usc.infolab.geom.shapefile;

import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;

import org.apache.commons.io.EndianUtils;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Code from github.com/mraad/Shapefile
 */
public class ShapeReader {
    private transient DataInputStream m_dataInputStream;
    private transient ShapeHeader m_shpHeader;

    private transient int m_parts[] = new int[4];

    public transient int recordNumber;
    public transient int contentLength;
    public transient int contentLengthInBytes;
    public transient int shapeType;
    public transient double xmin;
    public transient double ymin;
    public transient double xmax;
    public transient double ymax;
    public transient double mmin;
    public transient double mmax;
    public transient int numParts;
    public transient int numPoints;

    public ShapeReader(final DataInputStream dataInputStream) throws IOException
    {
        m_dataInputStream = dataInputStream;
        m_shpHeader = new ShapeHeader(dataInputStream);
    }

    public ShapeHeader getHeader()
    {
        return m_shpHeader;
    }

    public boolean hasMore() throws IOException
    {
        return m_dataInputStream.available() > 0;
    }

    private void readRecordHeader() throws IOException
    {
        recordNumber = m_dataInputStream.readInt();
        contentLength = m_dataInputStream.readInt();
        contentLengthInBytes = contentLength + contentLength - 4;

        shapeType = EndianUtils.readSwappedInteger(m_dataInputStream);
    }

    public Point readPoint() throws IOException
    {
        return queryPoint(new Point());
    }

    public Polygon readPolygon() throws IOException
    {
        return queryPolygon(new Polygon());
    }

    public Point queryPoint(final Point point) throws IOException
    {
        readRecordHeader();
        point.setX(EndianUtils.readSwappedDouble(m_dataInputStream));
        point.setY(EndianUtils.readSwappedDouble(m_dataInputStream));
        return point;
    }

    public Polygon queryPolygon(final Polygon polygon) throws IOException
    {
        polygon.setEmpty();

        readRecordHeader();

        readShapeHeader();

        for (int i = 0, j = 1; i < numParts; )
        {
            final int count = m_parts[j++] - m_parts[i++];
            for (int c = 0; c < count; c++)
            {
                final double x = EndianUtils.readSwappedDouble(m_dataInputStream);
                final double y = EndianUtils.readSwappedDouble(m_dataInputStream);
                if (c > 0)
                {
                    polygon.lineTo(x, y);
                }
                else
                {
                    polygon.startPath(x, y);
                }
            }
        }

        polygon.closeAllPaths();

        return polygon;
    }

    private void readShapeHeader() throws IOException
    {
        xmin = EndianUtils.readSwappedDouble(m_dataInputStream);
        ymin = EndianUtils.readSwappedDouble(m_dataInputStream);
        xmax = EndianUtils.readSwappedDouble(m_dataInputStream);
        ymax = EndianUtils.readSwappedDouble(m_dataInputStream);

        numParts = EndianUtils.readSwappedInteger(m_dataInputStream);
        numPoints = EndianUtils.readSwappedInteger(m_dataInputStream);

        if ((numParts + 1) > m_parts.length)
        {
            m_parts = new int[numParts + 1];
        }
        for (int p = 0; p < numParts; p++)
        {
            m_parts[p] = EndianUtils.readSwappedInteger(m_dataInputStream);
        }
        m_parts[numParts] = numPoints;
    }

}
