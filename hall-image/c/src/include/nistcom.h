/*******************************************************************************

License: 
This software was developed at the National Institute of Standards and 
Technology (NIST) by employees of the Federal Government in the course 
of their official duties. Pursuant to title 17 Section 105 of the 
United States Code, this software is not subject to copyright protection 
and is in the public domain. NIST assumes no responsibility  whatsoever for 
its use by other parties, and makes no guarantees, expressed or implied, 
about its quality, reliability, or any other characteristic. 

This software has been determined to be outside the scope of the EAR
(see Part 734.3 of the EAR for exact details) as it has been created solely
by employees of the U.S. Government; it is freely distributed with no
licensing requirements; and it is considered public domain.  Therefore,
it is permissible to distribute this software as a free download from the
internet.

Disclaimer: 
This software was developed to promote biometric standards and biometric
technology testing for the Federal Government in accordance with the USA
PATRIOT Act and the Enhanced Border Security and Visa Entry Reform Act.
Specific hardware and software products identified in this software were used
in order to perform the software development.  In no case does such
identification imply recommendation or endorsement by the National Institute
of Standards and Technology, nor does it imply that the products and equipment
identified are necessarily the best available for the purpose.  

*******************************************************************************/


#ifndef _NISTCOM_H
#define _NISTCOM_H

//#ifndef _IHEAD_H
//#include <ihead.h>
//#endif

#ifndef _FET_H
#include "fet.h"
typedef FET NISTCOM;
#endif

#define NCM_EXT         "ncm"
//#define NCM_HEADER      "NIST_COM"        /* manditory */
#define NCM_HEADER      "GFS_COM"        /* manditory */
#define NCM_PIX_WIDTH   "PIX_WIDTH"       /* manditory */
#define NCM_PIX_HEIGHT  "PIX_HEIGHT"      /* manditory */
#define NCM_PIX_DEPTH   "PIX_DEPTH"       /* 1,8,24 (manditory)*/
#define NCM_PPI         "PPI"             /* -1 if unknown (manditory)*/
#define NCM_COLORSPACE  "COLORSPACE"      /* RGB,YCbCr,GRAY */
#define NCM_N_CMPNTS    "NUM_COMPONENTS"  /* [1..4] (manditory w/hv_factors)*/
#define NCM_HV_FCTRS    "HV_FACTORS"      /* H0,V0:H1,V1:...*/
#define NCM_INTRLV      "INTERLEAVE"      /* 0,1 (manditory w/depth=24) */
#define NCM_COMPRESSION "COMPRESSION"     /* NONE,JPEGB,JPEGL,WSQ */
#define NCM_JPEGB_QUAL  "JPEGB_QUALITY"   /* [20..95] */
#define NCM_JPEGL_PREDICT "JPEGL_PREDICT" /* [1..7] */
#define NCM_WSQ_RATE    "WSQ_BITRATE"     /* ex. .75,2.25 (-1.0 if unknown)*/
#define NCM_LOSSY       "LOSSY"           /* 0,1 */

#define NCM_HISTORY     "HISTORY"         /* ex. SD historical data */
#define NCM_FING_CLASS  "FING_CLASS"      /* ex. A,L,R,S,T,W */
#define NCM_SEX         "SEX"             /* m,f */
#define NCM_SCAN_TYPE   "SCAN_TYPE"       /* l,i */
#define NCM_FACE_POS    "FACE_POS"        /* f,p */
#define NCM_AGE         "AGE"
#define NCM_SD_ID       "SD_ID"           /* 4,9,10,14,18 */


/* nistcom.c */
extern int combine_nistcom(NISTCOM **, const int, const int,
                           const int, const int, const int);
extern int combine_jpegl_nistcom(NISTCOM **, const int, const int,
                           const int, const int, const int, const int,
                           int *, int *, const int, const int);
extern int combine_wsq_nistcom(NISTCOM **, const int, const int,
                           const int, const int, const int, const float);
extern int combine_jpegb_nistcom(NISTCOM **, const int, const int,
                           const int, const int, const int,
                           char *, const int, const int, const int);
extern int del_jpegl_nistcom(NISTCOM *);
extern int del_wsq_nistcom(NISTCOM *);
extern int del_jpegb_nistcom(NISTCOM *);
extern int add_jpegb_nistcom(NISTCOM *, const int);
extern int add_jpegl_nistcom(NISTCOM *, const int, int *, int *,
                           const int, const int);
extern int add_wsq_nistcom(NISTCOM *);
//extern int sd_ihead_to_nistcom(NISTCOM **, IHEAD *, int);
//extern int sd4_ihead_to_nistcom(NISTCOM **, IHEAD *);
//extern int sd9_10_14_ihead_to_nistcom(NISTCOM **, IHEAD *, const int);
//extern int sd18_ihead_to_nistcom(NISTCOM **, IHEAD *);
extern int get_sd_class(char *, const int, char *);
extern int get_class_from_ncic_class_string(char *, const int, char *);


#endif /* !_NISTCOM_H */
