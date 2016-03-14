XPRT Women Code-A-Thon (1st Place)
==================================
Viveret Steele
Sunday March 13th, 2016

My Submission
=============
This workload focuses on the GPU and OpenGL by loading in .ply meshes and displaying them.

It measures the time it takes to:

  * Allocate multiple blocks of memory on the main RAM (with `new`) and GPU vram (with `glBufferData`)
  * Upload and update data (vertices, colors, indices) to the GPU using `glBufferSubData`
  * Render said VBOs (Vertex Buffer Objects) using `glDrawElements` and indexed vertices
  * Reallocate new VBOs when switching scenes / meshes

Credits:
--------

  * Uses an Android friendly (rewritten Apache code) version of Jply to load meshes

(./docs/Screenshot_2016-03-13-19-46-55.png)

About ChickTech
===============
ChickTech envisions a safe, inclusive, and innovative technology future that includes equal pay, participation, and treatment of women. It is dedicated to retaining women in the technology workforce and increasing the number of women and girls pursuing technology-based careers. For more information, please visit http://seattle.chicktech.org

About Principled Technologies, Inc.
===================================
Principled Technologies, Inc. is a leading provider of technology marketing and learning & development services. It administers the BenchmarkXPRT Development Community. Principled Technologies, Inc. is located in Durham, North Carolina, USA. For more information, please visit www.principledtechnologies.com.

About the BenchmarkXPRT Development Community
=============================================
The BenchmarkXPRT Development Community is a forum where registered members can contribute to the process of creating and improving the XPRTs. For more information, please visit http://www.principledtechnologies.com/hdxprt/forum/register.php
