//- Copyright © 2008-2011 8th Light, Inc. All Rights Reserved.
//- Limelight and all included source files are distributed under terms of the MIT License.

package limelight.io;

import limelight.LimelightException;

import java.io.*;

public class StreamReader
{
	private final InputStream input;
	private State state;

	private final ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
	private OutputStream output;

	private int readGoal;
	private int readStatus;

	private boolean eof = false;

	private byte[] boundary;
	private int boundaryLength;
	private int matchingBoundaryIndex;
	private byte[] matchedBoundaryBytes;

	private long bytesConsumed;

	public StreamReader(InputStream input)
	{
		this.input = input;
	}

	public void close()
	{
    try
    {
      input.close();
    }
    catch(IOException e)
    {
      throw new LimelightException(e);
    }
  }

	public String readLine()
	{
		return bytesToString(readLineBytes());
	}

	public byte[] readLineBytes()
	{
		state = READLINE_STATE;
		return preformRead();
	}

	public String read(int count)
	{
		return bytesToString(readBytes(count));
	}

  public String readAll()
  {
    return bytesToString(readAllBytes());
  }

	public byte[] readBytes(int count)
	{
		readGoal = count;
		readStatus = 0;
		state = READCOUNT_STATE;
		return preformRead();
	}

	public byte[] readAllBytes()
	{
		state = READALL_STATE;
		return preformRead();
	}

	public void copyBytes(int count, OutputStream output)
	{
		readGoal = count;
		state = READCOUNT_STATE;
		performCopy(output);
	}

	public String readUpTo(String boundary)
	{
		return bytesToString(readBytesUpTo(boundary));
	}

	public byte[] readBytesUpTo(String boundary)
	{
		prepareForReadUpTo(boundary);
		return preformRead();
	}

	private void prepareForReadUpTo(String boundary)
	{
		this.boundary = boundary.getBytes();
		boundaryLength = this.boundary.length;
		matchedBoundaryBytes = new byte[boundaryLength];
		matchingBoundaryIndex = 0;
		state = READUPTO_STATE;
	}

	public void copyBytesUpTo(String boundary, OutputStream outputStream)
	{
		prepareForReadUpTo(boundary);
		performCopy(outputStream);
	}

	public int byteCount()
	{
		return byteBuffer.size();
	}

	public byte[] getBufferedBytes()
	{
		return byteBuffer.toByteArray();
	}

	private byte[] preformRead()
	{
		setReadMode();
		clearBuffer();
		readUntilFinished();
		return getBufferedBytes();
	}

	private void performCopy(OutputStream output)
	{
		setCopyMode(output);
		readUntilFinished();
	}

	private void readUntilFinished()
	{
		while(!state.finished())
			state.read(input);
	}

	private void clearBuffer()
	{
		byteBuffer.reset();
	}

	private void setCopyMode(OutputStream output)
	{
		this.output = output;
	}

	private void setReadMode()
	{
		output = byteBuffer;
	}

	private String bytesToString(byte[] bytes)
	{
    try
    {
      return new String(bytes, "UTF-8");
    }
    catch(UnsupportedEncodingException e)
    {
      throw new LimelightException(e);
    }
  }

	private void changeState(State state)
	{
		this.state = state;
	}

	public boolean isEof()
	{
		return eof;
	}

	public long numberOfBytesConsumed()
	{
		return bytesConsumed;
	}

	public void resetNumberOfBytesConsumed()
	{
		bytesConsumed = 0;
	}

	private static abstract class State
	{
		public void read(InputStream input)
		{
		}

		public boolean finished()
		{
			return false;
		}
	}

	private final State READLINE_STATE = new State()
	{
		public void read(InputStream input)
		{
      try
      {
        int b = input.read();
        if(b == -1)
        {
          changeState(FINAL_STATE);
          eof = true;
        }
        else
        {
          bytesConsumed++;
          if(b == '\n')
            changeState(FINAL_STATE);
          else if(b != '\r')
            output.write((byte) b);
        }
      }
      catch(IOException e)
      {
        throw new LimelightException(e);
      }
    }
	};

	private final State READCOUNT_STATE = new State()
	{
		public void read(InputStream input)
		{
      try
      {
        byte[] bytes = new byte[readGoal - readStatus];
        int bytesRead = input.read(bytes);

        if(bytesRead < 0)
        {
          changeState(FINAL_STATE);
          eof = true;
        }
        else
        {
          bytesConsumed += bytesRead;
          readStatus += bytesRead;
          output.write(bytes, 0, bytesRead);
        }
      }
      catch(IOException e)
      {
        throw new LimelightException(e);
      }
    }

		public boolean finished()
		{
			return readStatus >= readGoal;
		}
	};

	private final State READALL_STATE = new State()
	{
		public void read(InputStream input)
		{
      try
      {
        byte[] bytes = new byte[1000];
        int bytesRead = input.read(bytes);

        if(bytesRead == -1)
        {
          changeState(FINAL_STATE);
          eof = true;
        }
        else
        {
          bytesConsumed += bytesRead;
          output.write(bytes, 0, bytesRead);
        }
      }
      catch(IOException e)
      {
        throw new LimelightException(e);
      }
    }

		public boolean finished()
		{
			return eof;
		}
	};

	private final State READUPTO_STATE = new State()
	{
		public void read(InputStream input)
		{
      try
      {
        int b = input.read();
        if(b == -1)
        {
          changeState(FINAL_STATE);
          eof = true;
        }
        else
        {
          bytesConsumed++;
          if(b == boundary[matchingBoundaryIndex])
          {
            matchedBoundaryBytes[matchingBoundaryIndex++] = (byte)b;
            if(matchingBoundaryIndex >= boundaryLength)
              changeState(FINAL_STATE);
          }
          else if(matchingBoundaryIndex == 0)
            output.write((byte)b);
          else
          {
            output.write(matchedBoundaryBytes, 0, matchingBoundaryIndex);
            matchingBoundaryIndex = 0;
            if(b == boundary[matchingBoundaryIndex])
              matchedBoundaryBytes[matchingBoundaryIndex++] = (byte)b;
            else
              output.write((byte)b);
          }
        }
      }
      catch(IOException e)
      {
        throw new LimelightException(e);
      }
    }
	};

	private final State FINAL_STATE = new State()
	{
		public boolean finished()
		{
			return true;
		}
	};
}
