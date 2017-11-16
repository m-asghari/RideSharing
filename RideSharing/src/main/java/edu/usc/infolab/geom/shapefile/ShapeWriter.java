package edu.usc.infolab.geom.shapefile;

import org.apache.commons.io.EndianUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by Mohammad on 11/13/2017.
 */
public class ShapeWriter {
    private transient DataInputStream m_dataInputStream;
    private transient ShapeHeader m_shpHeader;

    private DataOutputStream m_dataOutputStream;

    public ShapeWriter(final DataInputStream dataInputStream, final String outputName) throws IOException
    {
        m_dataInputStream = dataInputStream;
        m_shpHeader = new ShapeHeader(dataInputStream);

        FileOutputStream fos = new FileOutputStream(outputName);
        m_dataOutputStream = new DataOutputStream(fos);
    }

    public void write(List<Integer> records) throws IOException {
        int fileLength = 0;
        int[] contentLengths = new int[records.size()];
        byte[][] rawRecords = new byte[records.size()][];

        int r = 0;
        while (r < contentLengths.length && m_dataInputStream.available() > 0) {
            int recordNumber = m_dataInputStream.readInt();
            int contentLength = m_dataInputStream.readInt();
            if (records.get(r) == recordNumber) {
                rawRecords[r] = new byte[2 * contentLength];
                contentLengths[r] = contentLength;
                fileLength += contentLength + 4;
                m_dataInputStream.read(rawRecords[r], 0, contentLength*2);
                r++;
            } else {
                m_dataInputStream.skip(2*contentLength);
            }
        }

        m_dataOutputStream.writeInt(9994);
        for (int i = 0; i < 5; i++)
            m_dataOutputStream.writeInt(0);
        m_dataOutputStream.writeInt(fileLength);
        EndianUtils.writeSwappedInteger(m_dataOutputStream, m_shpHeader.version);
        EndianUtils.writeSwappedInteger(m_dataOutputStream, m_shpHeader.shapeType);
        EndianUtils.writeSwappedDouble(m_dataOutputStream, m_shpHeader.xmin);
        EndianUtils.writeSwappedDouble(m_dataOutputStream, m_shpHeader.ymin);
        EndianUtils.writeSwappedDouble(m_dataOutputStream, m_shpHeader.xmax);
        EndianUtils.writeSwappedDouble(m_dataOutputStream, m_shpHeader.ymax);
        EndianUtils.writeSwappedDouble(m_dataOutputStream, m_shpHeader.zmin);
        EndianUtils.writeSwappedDouble(m_dataOutputStream, m_shpHeader.zmax);
        EndianUtils.writeSwappedDouble(m_dataOutputStream, m_shpHeader.mmin);
        EndianUtils.writeSwappedDouble(m_dataOutputStream, m_shpHeader.mmax);

        for (int i = 0; i < rawRecords.length; i++) {
            m_dataOutputStream.writeInt(i+1);
            m_dataOutputStream.writeInt(contentLengths[i]);
            m_dataOutputStream.write(rawRecords[i]);
        }

        m_dataOutputStream.flush();
        m_dataOutputStream.close();
    }
}
