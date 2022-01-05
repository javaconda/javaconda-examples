/*******************************************************************************
 * Copyright (C) 2021, Ko Sugawara
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package org.javaconda.example;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.javaconda.Conda;

/**
 * This example illustrates how to install and run Cellpose using Javaconda.
 * 
 */
public class Cellpose
{

	/**
	 * This main method runs the following commands:
	 * <p>
	 * <ul>
	 * <li>Create a {@link Conda} instance</li>
	 * <li>Install Cellpose to the {@code cellpose} environment (if not exist)</li>
	 * <li>Download a sample image</li>
	 * <li>Run Cellpose inference</li>
	 * </ul>
	 * </p>
	 *
	 * @param args
	 *            not Used
	 * @throws IOException
	 *             If an I/O error occurs.
	 * @throws InterruptedException
	 *             If the current thread is interrupted by another thread while it
	 *             is waiting, then the wait is ended and an InterruptedException is
	 *             thrown.
	 */
	public static void main( final String... args ) throws IOException, InterruptedException
	{
		final Conda conda = new Conda( "/mnt/md0/applications/javaconda" );
		final String envName = "cellpose";
		if ( !conda.getEnvironmentNames().contains( envName ) )
		{
			conda.create( envName, "python=3.8" );
			conda.activate( envName );
			conda.pipInstall( "cellpose==0.7.2" );
			conda.pipUninstall( "torch" );
			conda.install( "pytorch", "cudatoolkit=10.2", "-c", "pytorch" );

			// Fix the issue #378:
			// https://github.com/MouseLand/cellpose/issues/378#issuecomment-976767543
			if ( SystemUtils.IS_OS_WINDOWS )
			{
				final Path path = Paths.get( conda.getRootdir(), "envs", "cellpose", "Lib", "site-packages", "cellpose", "dynamics.py" );
				final List< String > lines = Files.readAllLines( path, StandardCharsets.UTF_8 );
				lines.set( 103, "    meds = torch.from_numpy(centers.astype(int)).to(torch.long).to(device)" );
				Files.write( path, lines, StandardCharsets.UTF_8 );
			}
		}
		conda.activate( envName );
		final File file = new File( "javaconda_workspace/cellpose/img02.png" );
		FileUtils.copyURLToFile(
				new URL( "http://www.cellpose.org/static/images/img02.png" ),
				file,
				10000,
				10000 );
		conda.runPython( "-m", "cellpose", "--use_gpu", "--dir", file.getParentFile().getAbsolutePath(), "--pretrained_model", "cyto", "--chan", "2", "--chan2", "3", "--save_png" );
	}

}
