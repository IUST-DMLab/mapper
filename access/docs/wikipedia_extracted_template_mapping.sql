-- phpMyAdmin SQL Dump
-- version 4.5.4.1deb2ubuntu2
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Mar 01, 2017 at 02:53 PM
-- Server version: 5.7.17-0ubuntu0.16.04.1
-- PHP Version: 7.0.15-0ubuntu0.16.04.2

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `knowledge_graph`
--

-- --------------------------------------------------------

--
-- Table structure for table `wikipedia_extracted_template_mapping`
--

CREATE TABLE `wikipedia_extracted_template_mapping` (
  `id` int(11) NOT NULL,
  `template_full_name_fa` varchar(1000) DEFAULT NULL,
  `template_full_name_en` varchar(1000) DEFAULT NULL,
  `template_name_fa` varchar(500) DEFAULT NULL,
  `template_name_en` varchar(500) DEFAULT NULL,
  `template_type_fa` varchar(500) DEFAULT NULL,
  `template_type_en` varchar(500) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `wikipedia_extracted_template_mapping`
--

INSERT INTO `wikipedia_extracted_template_mapping` (`id`, `template_full_name_fa`, `template_full_name_en`, `template_name_fa`, `template_name_en`, `template_type_fa`, `template_type_en`) VALUES
(1, 'جعبه آب و هوایی دبی', 'infobox settlement', 'آب و هوایی دبی', 'settlement', 'جعبه', 'infobox'),
(2, 'جعبه اطلاعات شهر ایران', 'infobox settlement', 'شهر ایران', 'settlement', 'جعبه اطلاعات', 'infobox'),
(3, 'جعبه اطلاعات کوکتل', 'infobox cocktail', 'کوکتل', 'cocktail', 'جعبه اطلاعات', 'infobox'),
(4, 'جعبه اطلاعات اربیم', 'infobox erbium', 'اربیم', 'erbium', 'جعبه اطلاعات', 'infobox'),
(5, 'جعبه اطلاعات منطقه یونان باستان', 'infobox ancient site', 'منطقه یونان باستان', 'ancient site', 'جعبه اطلاعات', 'infobox'),
(6, 'جعبه اطلاعات فلوئور', 'infobox fluorine', 'فلوئور', 'fluorine', 'جعبه اطلاعات', 'infobox'),
(7, 'جعبه اطلاعات هاسیم', 'infobox hassium', 'هاسیم', 'hassium', 'جعبه اطلاعات', 'infobox'),
(8, 'جعبه اطلاعات نپتونیم', 'infobox neptunium', 'نپتونیم', 'neptunium', 'جعبه اطلاعات', 'infobox'),
(9, 'جعبه شهرهای نیوزیلند', 'infobox settlement', 'شهرهای نیوزیلند', 'settlement', 'جعبه', 'infobox'),
(10, 'جعبه اطلاعات گادولینیم', 'infobox gadolinium', 'گادولینیم', 'gadolinium', 'جعبه اطلاعات', 'infobox'),
(11, 'جعبه اطلاعات کروم', 'infobox chromium', 'کروم', 'chromium', 'جعبه اطلاعات', 'infobox'),
(12, 'جعبه اطلاعات اورانیم', 'infobox uranium', 'اورانیم', 'uranium', 'جعبه اطلاعات', 'infobox'),
(13, 'جعبه زیستگاه', 'infobox settlement', 'زیستگاه', 'settlement', 'جعبه', 'infobox'),
(14, 'جعبه زیستگاه', 'infobox hungarian settlement', 'زیستگاه', 'hungarian settlement', 'جعبه', 'infobox'),
(15, 'جعبه اطلاعات گالیم', 'infobox gallium', 'گالیم', 'gallium', 'جعبه اطلاعات', 'infobox'),
(16, 'جعبه اطلاعات برند', 'infobox brand', 'برند', 'brand', 'جعبه اطلاعات', 'infobox'),
(17, 'جعبه اطلاعات فرودگاه', 'infobox airport', 'فرودگاه', 'airport', 'جعبه اطلاعات', 'infobox'),
(18, 'جعبه حوزه فرانسه', 'infobox department', 'حوزه فرانسه', 'department', 'جعبه', 'infobox'),
(19, 'جعبه اطلاعات پولونیم', 'infobox polonium', 'پولونیم', 'polonium', 'جعبه اطلاعات', 'infobox'),
(20, 'جعبه اطلاعات لانتان', 'infobox lanthanum', 'لانتان', 'lanthanum', 'جعبه اطلاعات', 'infobox'),
(21, 'جعبه اطلاعات تنگستن', 'infobox tungsten', 'تنگستن', 'tungsten', 'جعبه اطلاعات', 'infobox'),
(22, 'جعبه اطلاعات تولیم', 'infobox thulium', 'تولیم', 'thulium', 'جعبه اطلاعات', 'infobox'),
(23, 'جعبه اطلاعات نقره', 'infobox silver', 'نقره', 'silver', 'جعبه اطلاعات', 'infobox'),
(24, 'جعبه اطلاعات مجله', 'infobox journal', 'مجله', 'journal', 'جعبه اطلاعات', 'infobox'),
(25, 'جعبه اطلاعات تکنسیم', 'infobox technetium', 'تکنسیم', 'technetium', 'جعبه اطلاعات', 'infobox'),
(26, 'جعبه اطلاعات ساماریم', 'infobox samarium', 'ساماریم', 'samarium', 'جعبه اطلاعات', 'infobox'),
(27, 'جعبه اطلاعات روستای ورجان', 'infobox settlement', 'روستای ورجان', 'settlement', 'جعبه اطلاعات', 'infobox'),
(28, 'جعبه اطلاعات تلوریم', 'infobox tellurium', 'تلوریم', 'tellurium', 'جعبه اطلاعات', 'infobox'),
(29, 'جعبه اطلاعات سیلسیم', 'infobox silicon', 'سیلسیم', 'silicon', 'جعبه اطلاعات', 'infobox'),
(30, 'جعبه اطلاعات سریم', 'infobox cerium', 'سریم', 'cerium', 'جعبه اطلاعات', 'infobox'),
(31, 'جعبه اطلاعات رادرفوردیم', 'infobox rutherfordium', 'رادرفوردیم', 'rutherfordium', 'جعبه اطلاعات', 'infobox'),
(32, 'جعبه اطلاعات تالیم', 'infobox thallium', 'تالیم', 'thallium', 'جعبه اطلاعات', 'infobox'),
(33, 'جعبه اطلاعات گروه‌های جنایی', 'infobox criminal organization', 'گروه‌های جنایی', 'criminal organization', 'جعبه اطلاعات', 'infobox'),
(34, 'جعبه ‌های گشت‌وگذار در مقاله‌ها', 'infobox russian federal subject', '‌های گشت‌وگذار در مقاله‌ها', 'russian federal subject', 'جعبه', 'infobox'),
(35, 'جعبه ‌های گشت‌وگذار در مقاله‌ها', 'infobox world heritage site', '‌های گشت‌وگذار در مقاله‌ها', 'world heritage site', 'جعبه', 'infobox'),
(36, 'جعبه ‌های گشت‌وگذار در مقاله‌ها', 'infobox settlement', '‌های گشت‌وگذار در مقاله‌ها', 'settlement', 'جعبه', 'infobox'),
(37, 'جعبه اطلاعات آلبوم', 'infobox album', 'آلبوم', 'album', 'جعبه اطلاعات', 'infobox'),
(38, 'جعبه اطلاعات لیتیم', 'infobox lithium', 'لیتیم', 'lithium', 'جعبه اطلاعات', 'infobox'),
(39, 'جعبه اطلاعات مداخله', 'infobox medical intervention', 'مداخله', 'medical intervention', 'جعبه اطلاعات', 'infobox'),
(40, 'جعبه اطلاعات آهن', 'infobox iron', 'آهن', 'iron', 'جعبه اطلاعات', 'infobox'),
(41, 'جعبه اطلاعات نیکل', 'infobox nickel', 'نیکل', 'nickel', 'جعبه اطلاعات', 'infobox'),
(42, 'جعبه اطلاعات تیتانیم', 'infobox titanium', 'تیتانیم', 'titanium', 'جعبه اطلاعات', 'infobox'),
(43, 'جعبه اطلاعات ماموریت فضایی', 'infobox spaceflight/dock', 'ماموریت فضایی', 'spaceflight/dock', 'جعبه اطلاعات', 'infobox'),
(44, 'جعبه اطلاعات ماموریت فضایی', 'infobox spaceflight', 'ماموریت فضایی', 'spaceflight', 'جعبه اطلاعات', 'infobox'),
(45, 'جعبه اطلاعات رنگ', 'infobox color', 'رنگ', 'color', 'جعبه اطلاعات', 'infobox'),
(46, 'جعبه اطلاعات رابط', 'infobox connector', 'رابط', 'connector', 'جعبه اطلاعات', 'infobox'),
(47, 'جعبه اطلاعات پایگاه‌های باستان‌شناسی', 'infobox settlement', 'پایگاه‌های باستان‌شناسی', 'settlement', 'جعبه اطلاعات', 'infobox'),
(48, 'جعبه اطلاعات پایگاه‌های باستان‌شناسی', 'infobox ancient site', 'پایگاه‌های باستان‌شناسی', 'ancient site', 'جعبه اطلاعات', 'infobox'),
(49, 'جعبه اطلاعات ساز', 'infobox instrument', 'ساز', 'instrument', 'جعبه اطلاعات', 'infobox'),
(50, 'جعبه اطلاعات اسکاندیم', 'infobox scandium', 'اسکاندیم', 'scandium', 'جعبه اطلاعات', 'infobox'),
(51, 'جعبه اطلاعات هیدروژن', 'infobox hydrogen', 'هیدروژن', 'hydrogen', 'جعبه اطلاعات', 'infobox'),
(52, 'جعبه شهر بورکینافاسو', 'infobox settlement', 'شهر بورکینافاسو', 'settlement', 'جعبه', 'infobox'),
(53, 'جعبه اطلاعات سازمان ملل', 'infobox united nations', 'سازمان ملل', 'united nations', 'جعبه اطلاعات', 'infobox'),
(54, 'جعبه اطلاعات پتاسیم', 'infobox potassium', 'پتاسیم', 'potassium', 'جعبه اطلاعات', 'infobox'),
(55, 'جعبه اطلاعات فرمان یونیکس', 'infobox software', 'فرمان یونیکس', 'software', 'جعبه اطلاعات', 'infobox'),
(56, 'جعبه اطلاعات فرمیم', 'infobox fermium', 'فرمیم', 'fermium', 'جعبه اطلاعات', 'infobox'),
(57, 'جعبه اطلاعات تربیم', 'infobox terbium', 'تربیم', 'terbium', 'جعبه اطلاعات', 'infobox'),
(58, 'جعبه اطلاعات بیماری', 'infobox medical condition (new)', 'بیماری', 'medical condition (new)', 'جعبه اطلاعات', 'infobox'),
(59, 'جعبه سمپتوم', 'infobox symptom', 'سمپتوم', 'symptom', 'جعبه', 'infobox'),
(60, 'جعبه اطلاعات ورزش', 'infobox sport', 'ورزش', 'sport', 'جعبه اطلاعات', 'infobox'),
(61, 'جعبه مشاغل', 'infobox occupation', 'مشاغل', 'occupation', 'جعبه', 'infobox'),
(62, 'جعبه شرکت', 'infobox company', 'شرکت', 'company', 'جعبه', 'infobox'),
(63, 'جعبه اطلاعات مغز', 'infobox brain', 'مغز', 'brain', 'جعبه اطلاعات', 'infobox'),
(64, 'جعبه نام  ستاره', 'starbox begin', 'نام  ستاره', 'begin', 'جعبه', 'starbox'),
(65, 'جعبه نام  ستاره', 'starbox character', 'نام  ستاره', 'character', 'جعبه', 'starbox'),
(66, 'جعبه نام  ستاره', 'starbox astrometry', 'نام  ستاره', 'astrometry', 'جعبه', 'starbox'),
(67, 'جعبه نام  ستاره', 'starbox detail', 'نام  ستاره', 'detail', 'جعبه', 'starbox'),
(68, 'جعبه نام  ستاره', 'starbox catalog', 'نام  ستاره', 'catalog', 'جعبه', 'starbox'),
(69, 'جعبه نام  ستاره', 'starbox reference', 'نام  ستاره', 'reference', 'جعبه', 'starbox'),
(70, 'جعبه نام  ستاره', 'starbox end', 'نام  ستاره', 'end', 'جعبه', 'starbox'),
(71, 'جعبه نام  ستاره', 'starbox observe', 'نام  ستاره', 'observe', 'جعبه', 'starbox'),
(72, 'جعبه نام  ستاره', 'starbox image', 'نام  ستاره', 'image', 'جعبه', 'starbox'),
(73, 'جعبه نام  ستاره', 'starbox observe 3s', 'نام  ستاره', 'observe 3s', 'جعبه', 'starbox'),
(74, 'جعبه اطلاعات نژاد سگ', 'infobox dogbreed', 'نژاد سگ', 'dogbreed', 'جعبه اطلاعات', 'infobox'),
(75, 'جعبه تفاصیل  ستاره', 'starbox begin', 'تفاصیل  ستاره', 'begin', 'جعبه', 'starbox'),
(76, 'جعبه تفاصیل  ستاره', 'starbox image', 'تفاصیل  ستاره', 'image', 'جعبه', 'starbox'),
(77, 'جعبه تفاصیل  ستاره', 'starbox character', 'تفاصیل  ستاره', 'character', 'جعبه', 'starbox'),
(78, 'جعبه تفاصیل  ستاره', 'starbox astrometry', 'تفاصیل  ستاره', 'astrometry', 'جعبه', 'starbox'),
(79, 'جعبه تفاصیل  ستاره', 'starbox catalog', 'تفاصیل  ستاره', 'catalog', 'جعبه', 'starbox'),
(80, 'جعبه تفاصیل  ستاره', 'starbox reference', 'تفاصیل  ستاره', 'reference', 'جعبه', 'starbox'),
(81, 'جعبه تفاصیل  ستاره', 'starbox end', 'تفاصیل  ستاره', 'end', 'جعبه', 'starbox'),
(82, 'جعبه تفاصیل  ستاره', 'starbox observe 3s', 'تفاصیل  ستاره', 'observe 3s', 'جعبه', 'starbox'),
(83, 'جعبه تفاصیل  ستاره', 'starbox detail', 'تفاصیل  ستاره', 'detail', 'جعبه', 'starbox'),
(84, 'جعبه اطلاعات پالادیم', 'infobox palladium', 'پالادیم', 'palladium', 'جعبه اطلاعات', 'infobox'),
(85, 'جعبه اطلاعات مس', 'infobox copper', 'مس', 'copper', 'جعبه اطلاعات', 'infobox'),
(86, 'جعبه اطلاعات سدیم', 'infobox sodium', 'سدیم', 'sodium', 'جعبه اطلاعات', 'infobox'),
(87, 'جعبه اطلاعات روبیدیم', 'infobox rubidium', 'روبیدیم', 'rubidium', 'جعبه اطلاعات', 'infobox'),
(88, 'جعبه اطلاعات نیتروژن', 'infobox nitrogen', 'نیتروژن', 'nitrogen', 'جعبه اطلاعات', 'infobox'),
(89, 'جعبه اطلاعات نرم‌افزار', 'infobox software', 'نرم‌افزار', 'software', 'جعبه اطلاعات', 'infobox'),
(90, 'جعبه اطلاعات هواپیما', 'infobox aircraft begin', 'هواپیما', 'aircraft begin', 'جعبه اطلاعات', 'infobox'),
(91, 'جعبه اطلاعات هواپیما', 'infobox aircraft type', 'هواپیما', 'aircraft type', 'جعبه اطلاعات', 'infobox'),
(92, 'جعبه اخترسنجی  ستاره', 'starbox begin', 'اخترسنجی  ستاره', 'begin', 'جعبه', 'starbox'),
(93, 'جعبه اخترسنجی  ستاره', 'starbox image', 'اخترسنجی  ستاره', 'image', 'جعبه', 'starbox'),
(94, 'جعبه اخترسنجی  ستاره', 'starbox character', 'اخترسنجی  ستاره', 'character', 'جعبه', 'starbox'),
(95, 'جعبه اخترسنجی  ستاره', 'starbox astrometry', 'اخترسنجی  ستاره', 'astrometry', 'جعبه', 'starbox'),
(96, 'جعبه اخترسنجی  ستاره', 'starbox catalog', 'اخترسنجی  ستاره', 'catalog', 'جعبه', 'starbox'),
(97, 'جعبه اخترسنجی  ستاره', 'starbox reference', 'اخترسنجی  ستاره', 'reference', 'جعبه', 'starbox'),
(98, 'جعبه اخترسنجی  ستاره', 'starbox end', 'اخترسنجی  ستاره', 'end', 'جعبه', 'starbox'),
(99, 'جعبه اخترسنجی  ستاره', 'starbox observe 3s', 'اخترسنجی  ستاره', 'observe 3s', 'جعبه', 'starbox'),
(100, 'جعبه اخترسنجی  ستاره', 'starbox detail', 'اخترسنجی  ستاره', 'detail', 'جعبه', 'starbox'),
(101, 'جعبه اخترسنجی  ستاره', 'starbox observe', 'اخترسنجی  ستاره', 'observe', 'جعبه', 'starbox'),
(102, 'جعبه اطلاعات ید', 'infobox iodine', 'ید', 'iodine', 'جعبه اطلاعات', 'infobox'),
(103, 'جعبه اطلاعات آن‌ان‌پنتیوم', 'infobox moscovium', 'آن‌ان‌پنتیوم', 'moscovium', 'جعبه اطلاعات', 'infobox'),
(104, 'جعبه اطلاعات دوبنیم', 'infobox dubnium', 'دوبنیم', 'dubnium', 'جعبه اطلاعات', 'infobox'),
(105, 'جعبه اطلاعات جیوه', 'infobox mercury', 'جیوه', 'mercury', 'جعبه اطلاعات', 'infobox'),
(106, 'جعبه اطلاعات یوروپیم', 'infobox europium', 'یوروپیم', 'europium', 'جعبه اطلاعات', 'infobox'),
(107, 'جعبه اطلاعات هنرمند موسیقی', 'infobox musical artist', 'هنرمند موسیقی', 'musical artist', 'جعبه اطلاعات', 'infobox'),
(108, 'جعبه شهر اردن', 'infobox settlement', 'شهر اردن', 'settlement', 'جعبه', 'infobox'),
(109, 'جعبه اطلاعات زبان برنامه‌نویسی', 'infobox programming language', 'زبان برنامه‌نویسی', 'programming language', 'جعبه اطلاعات', 'infobox'),
(110, 'جعبه اطلاعات موجود فراطبیعی', 'infobox mythical creature', 'موجود فراطبیعی', 'mythical creature', 'جعبه اطلاعات', 'infobox'),
(111, 'جعبه اطلاعات نیهونیوم', 'infobox nihonium', 'نیهونیوم', 'nihonium', 'جعبه اطلاعات', 'infobox'),
(112, 'جعبه اطلاعات مناطق ایران', 'infobox settlement', 'مناطق ایران', 'settlement', 'جعبه اطلاعات', 'infobox'),
(113, 'جعبه رصد  ستاره ۲', 'starbox begin', 'رصد  ستاره ۲', 'begin', 'جعبه', 'starbox'),
(114, 'جعبه رصد  ستاره ۲', 'starbox character', 'رصد  ستاره ۲', 'character', 'جعبه', 'starbox'),
(115, 'جعبه رصد  ستاره ۲', 'starbox astrometry', 'رصد  ستاره ۲', 'astrometry', 'جعبه', 'starbox'),
(116, 'جعبه رصد  ستاره ۲', 'starbox detail', 'رصد  ستاره ۲', 'detail', 'جعبه', 'starbox'),
(117, 'جعبه رصد  ستاره ۲', 'starbox catalog', 'رصد  ستاره ۲', 'catalog', 'جعبه', 'starbox'),
(118, 'جعبه رصد  ستاره ۲', 'starbox reference', 'رصد  ستاره ۲', 'reference', 'جعبه', 'starbox'),
(119, 'جعبه رصد  ستاره ۲', 'starbox end', 'رصد  ستاره ۲', 'end', 'جعبه', 'starbox'),
(120, 'جعبه رصد  ستاره ۲', 'starbox observe 2s', 'رصد  ستاره ۲', 'observe 2s', 'جعبه', 'starbox'),
(121, 'جعبه اطلاعات مکان‌های انگلستان', 'infobox uk place', 'مکان‌های انگلستان', 'uk place', 'جعبه اطلاعات', 'infobox'),
(122, 'جعبه اطلاعات نئودیمیم', 'infobox neodymium', 'نئودیمیم', 'neodymium', 'جعبه اطلاعات', 'infobox'),
(123, 'جعبه اطلاعات روتنیم', 'infobox ruthenium', 'روتنیم', 'ruthenium', 'جعبه اطلاعات', 'infobox'),
(124, 'جعبه اطلاعات پردازنده', 'infobox cpu architecture', 'پردازنده', 'cpu architecture', 'جعبه اطلاعات', 'infobox'),
(125, 'جعبه اطلاعات پنیر', 'infobox cheese', 'پنیر', 'cheese', 'جعبه اطلاعات', 'infobox'),
(126, 'جعبه اطلاعات فسفر', 'infobox phosphorus', 'فسفر', 'phosphorus', 'جعبه اطلاعات', 'infobox'),
(127, 'جعبه اطلاعات میراث فرهنگی و معنوی بشر', 'infobox intangible heritage', 'میراث فرهنگی و معنوی بشر', 'intangible heritage', 'جعبه اطلاعات', 'infobox'),
(128, 'جعبه شهر ترکمنستان', 'infobox settlement', 'شهر ترکمنستان', 'settlement', 'جعبه', 'infobox'),
(129, 'جعبه اطلاعات روستای ایران', 'infobox settlement', 'روستای ایران', 'settlement', 'جعبه اطلاعات', 'infobox'),
(130, 'جعبه اطلاعات ایندیم', 'infobox indium', 'ایندیم', 'indium', 'جعبه اطلاعات', 'infobox'),
(131, 'جعبه اطلاعات بیوگرافی دینی', 'infobox religious biography', 'بیوگرافی دینی', 'religious biography', 'جعبه اطلاعات', 'infobox'),
(132, 'جعبه اطلاعات روی', 'infobox zinc', 'روی', 'zinc', 'جعبه اطلاعات', 'infobox'),
(133, 'جعبه اطلاعات منگنز', 'infobox manganese', 'منگنز', 'manganese', 'جعبه اطلاعات', 'infobox'),
(134, 'جعبه شهر افغانستان', 'infobox settlement', 'شهر افغانستان', 'settlement', 'جعبه', 'infobox'),
(135, 'جعبه اطلاعات رنیم', 'infobox rhenium', 'رنیم', 'rhenium', 'جعبه اطلاعات', 'infobox'),
(136, 'جعبه اطلاعات توریم', 'infobox thorium', 'توریم', 'thorium', 'جعبه اطلاعات', 'infobox'),
(137, 'جعبه اطلاعات نیوبیم', 'infobox niobium', 'نیوبیم', 'niobium', 'جعبه اطلاعات', 'infobox'),
(138, 'جعبه مکان‌های سرزمین میانه', 'infobox fictional location', 'مکان‌های سرزمین میانه', 'fictional location', 'جعبه', 'infobox'),
(139, 'جعبه اطلاعات اسمیم', 'infobox osmium', 'اسمیم', 'osmium', 'جعبه اطلاعات', 'infobox'),
(140, 'جعبه میراث جهانی یونسکو', 'infobox ancient site', 'میراث جهانی یونسکو', 'ancient site', 'جعبه', 'infobox'),
(141, 'جعبه اطلاعات ناحیه یونان', 'infobox settlement', 'ناحیه یونان', 'settlement', 'جعبه اطلاعات', 'infobox'),
(142, 'جعبه اطلاعات اینشتینیم', 'infobox einsteinium', 'اینشتینیم', 'einsteinium', 'جعبه اطلاعات', 'infobox'),
(143, 'جعبه اطلاعات گیاه‌شناسی محصول', 'infobox botanical product', 'گیاه‌شناسی محصول', 'botanical product', 'جعبه اطلاعات', 'infobox'),
(144, 'جعبه شهر تاجیکستان', 'infobox settlement', 'شهر تاجیکستان', 'settlement', 'جعبه', 'infobox'),
(145, 'جعبه اطلاعات رادون', 'infobox radon', 'رادون', 'radon', 'جعبه اطلاعات', 'infobox'),
(146, 'جعبه اطلاعات پروتاکتینیم', 'infobox protactinium', 'پروتاکتینیم', 'protactinium', 'جعبه اطلاعات', 'infobox'),
(147, 'جعبه اطلاعات شهرهای روسیه', 'infobox russian district', 'شهرهای روسیه', 'russian district', 'جعبه اطلاعات', 'infobox'),
(148, 'جعبه اطلاعات شهرهای روسیه', 'infobox NULL', 'شهرهای روسیه', 'NULL', 'جعبه اطلاعات', 'infobox'),
(149, 'جعبه شهر اسرائیل', 'infobox israel municipality', 'شهر اسرائیل', 'israel municipality', 'جعبه', 'infobox'),
(150, 'جعبه اطلاعات سلنیم', 'infobox selenium', 'سلنیم', 'selenium', 'جعبه اطلاعات', 'infobox'),
(151, 'جعبه اطلاعات طلا', 'infobox gold', 'طلا', 'gold', 'جعبه اطلاعات', 'infobox'),
(152, 'جعبه اطلاعات دارمشتادیم', 'infobox darmstadtium', 'دارمشتادیم', 'darmstadtium', 'جعبه اطلاعات', 'infobox'),
(153, 'جعبه اطلاعات اکسیژن', 'infobox oxygen', 'اکسیژن', 'oxygen', 'جعبه اطلاعات', 'infobox'),
(154, 'جعبه اطلاعات هولمیم', 'infobox holmium', 'هولمیم', 'holmium', 'جعبه اطلاعات', 'infobox'),
(155, 'جعبه قسمت مجموعه تلویزیونی', 'infobox television episode', 'قسمت مجموعه تلویزیونی', 'television episode', 'جعبه', 'infobox'),
(156, 'جعبه اطلاعات مایتنریم', 'infobox meitnerium', 'مایتنریم', 'meitnerium', 'جعبه اطلاعات', 'infobox'),
(157, 'جعبه اطلاعات اجزای ویندوز', 'infobox windows component', 'اجزای ویندوز', 'windows component', 'جعبه اطلاعات', 'infobox'),
(158, 'جعبه اطلاعات تنسین', 'infobox tennessine', 'تنسین', 'tennessine', 'جعبه اطلاعات', 'infobox'),
(159, 'جعبه اطلاعات کلر', 'infobox chlorine', 'کلر', 'chlorine', 'جعبه اطلاعات', 'infobox'),
(160, 'جعبه اطلاعات کلر', 'infobox e number  920-929', 'کلر', 'e number  920-929', 'جعبه اطلاعات', 'infobox'),
(161, 'جعبه اطلاعات سیاره های خیالی', 'infobox fictional planet', 'سیاره های خیالی', 'fictional planet', 'جعبه اطلاعات', 'infobox'),
(162, 'جعبه اطلاعات پرومتیم', 'infobox promethium', 'پرومتیم', 'promethium', 'جعبه اطلاعات', 'infobox'),
(163, 'جعبه اطلاعات اوگانسون', 'infobox oganesson', 'اوگانسون', 'oganesson', 'جعبه اطلاعات', 'infobox'),
(164, 'جعبه اطلاعات فلروویوم', 'infobox flerovium', 'فلروویوم', 'flerovium', 'جعبه اطلاعات', 'infobox'),
(165, 'جعبه اطلاعات منیزیم', 'infobox magnesium', 'منیزیم', 'magnesium', 'جعبه اطلاعات', 'infobox'),
(166, 'جعبه اطلاعات مندلیفیم', 'infobox mendelevium', 'مندلیفیم', 'mendelevium', 'جعبه اطلاعات', 'infobox'),
(167, 'جعبه پایین  کاربر', 'taxobox NULL', 'پایین  کاربر', 'NULL', 'جعبه', 'taxobox'),
(168, 'جعبه اطلاعات هافنیم', 'infobox hafnium', 'هافنیم', 'hafnium', 'جعبه اطلاعات', 'infobox'),
(169, 'جعبه اطلاعات علائم بیماری', 'infobox symptom', 'علائم بیماری', 'symptom', 'جعبه اطلاعات', 'infobox'),
(170, 'جعبه اطلاعات شهر غیر ایرانی', 'infobox uk place', 'شهر غیر ایرانی', 'uk place', 'جعبه اطلاعات', 'infobox'),
(171, 'جعبه اطلاعات اماکن استرالیا', 'infobox australian place', 'اماکن استرالیا', 'australian place', 'جعبه اطلاعات', 'infobox'),
(172, 'جعبه اطلاعات ایریدیم', 'infobox iridium', 'ایریدیم', 'iridium', 'جعبه اطلاعات', 'infobox'),
(173, 'جعبه اطلاعات فرانسیم', 'infobox francium', 'فرانسیم', 'francium', 'جعبه اطلاعات', 'infobox'),
(174, 'جعبه اطلاعات پرازئودیمیم', 'infobox praseodymium', 'پرازئودیمیم', 'praseodymium', 'جعبه اطلاعات', 'infobox'),
(175, 'جعبه اطلاعات نئون', 'infobox neon', 'نئون', 'neon', 'جعبه اطلاعات', 'infobox'),
(176, 'جعبه منبع  ستاره', 'starbox begin', 'منبع  ستاره', 'begin', 'جعبه', 'starbox'),
(177, 'جعبه منبع  ستاره', 'starbox image', 'منبع  ستاره', 'image', 'جعبه', 'starbox'),
(178, 'جعبه منبع  ستاره', 'starbox character', 'منبع  ستاره', 'character', 'جعبه', 'starbox'),
(179, 'جعبه منبع  ستاره', 'starbox astrometry', 'منبع  ستاره', 'astrometry', 'جعبه', 'starbox'),
(180, 'جعبه منبع  ستاره', 'starbox catalog', 'منبع  ستاره', 'catalog', 'جعبه', 'starbox'),
(181, 'جعبه منبع  ستاره', 'starbox reference', 'منبع  ستاره', 'reference', 'جعبه', 'starbox'),
(182, 'جعبه منبع  ستاره', 'starbox end', 'منبع  ستاره', 'end', 'جعبه', 'starbox'),
(183, 'جعبه منبع  ستاره', 'starbox observe 3s', 'منبع  ستاره', 'observe 3s', 'جعبه', 'starbox'),
(184, 'جعبه منبع  ستاره', 'starbox detail', 'منبع  ستاره', 'detail', 'جعبه', 'starbox'),
(185, 'جعبه اطلاعات غیرانتفاعی', 'infobox organization', 'غیرانتفاعی', 'organization', 'جعبه اطلاعات', 'infobox'),
(186, 'جعبه اطلاعات گوگرد', 'infobox sulfur', 'گوگرد', 'sulfur', 'جعبه اطلاعات', 'infobox'),
(187, 'جعبه اطلاعات گوگرد', 'infobox drug', 'گوگرد', 'drug', 'جعبه اطلاعات', 'infobox'),
(188, 'جعبه رصد  ستاره', 'starbox begin', 'رصد  ستاره', 'begin', 'جعبه', 'starbox'),
(189, 'جعبه رصد  ستاره', 'starbox character', 'رصد  ستاره', 'character', 'جعبه', 'starbox'),
(190, 'جعبه رصد  ستاره', 'starbox astrometry', 'رصد  ستاره', 'astrometry', 'جعبه', 'starbox'),
(191, 'جعبه رصد  ستاره', 'starbox detail', 'رصد  ستاره', 'detail', 'جعبه', 'starbox'),
(192, 'جعبه رصد  ستاره', 'starbox catalog', 'رصد  ستاره', 'catalog', 'جعبه', 'starbox'),
(193, 'جعبه رصد  ستاره', 'starbox reference', 'رصد  ستاره', 'reference', 'جعبه', 'starbox'),
(194, 'جعبه رصد  ستاره', 'starbox end', 'رصد  ستاره', 'end', 'جعبه', 'starbox'),
(195, 'جعبه رصد  ستاره', 'starbox observe', 'رصد  ستاره', 'observe', 'جعبه', 'starbox'),
(196, 'جعبه رصد  ستاره', 'starbox image', 'رصد  ستاره', 'image', 'جعبه', 'starbox'),
(197, 'جعبه رصد  ستاره', 'starbox observe 3s', 'رصد  ستاره', 'observe 3s', 'جعبه', 'starbox'),
(198, 'جعبه اطلاعات مولفه الکترونیک', 'infobox electronic component', 'مولفه الکترونیک', 'electronic component', 'جعبه اطلاعات', 'infobox'),
(199, 'جعبه اطلاعات رودیم', 'infobox rhodium', 'رودیم', 'rhodium', 'جعبه اطلاعات', 'infobox'),
(200, 'جعبه اطلاعات وب‌گاه', 'infobox website', 'وب‌گاه', 'website', 'جعبه اطلاعات', 'infobox'),
(201, 'جعبه اطلاعات ایزد هندو', 'infobox deity', 'ایزد هندو', 'deity', 'جعبه اطلاعات', 'infobox'),
(202, 'جعبه اطلاعات سیبورگیم', 'infobox seaborgium', 'سیبورگیم', 'seaborgium', 'جعبه اطلاعات', 'infobox'),
(203, 'جعبه اطلاعات غذای آماده', 'infobox prepared food', 'غذای آماده', 'prepared food', 'جعبه اطلاعات', 'infobox'),
(204, 'جعبه دامنه سطح بالا', 'infobox top level domain', 'دامنه سطح بالا', 'top level domain', 'جعبه', 'infobox'),
(205, 'جعبه اطلاعات لیورموریوم', 'infobox livermorium', 'لیورموریوم', 'livermorium', 'جعبه اطلاعات', 'infobox'),
(206, 'جعبه شهر پاکستان', 'infobox settlement', 'شهر پاکستان', 'settlement', 'جعبه', 'infobox'),
(207, 'جعبه آرایه زیستی', 'taxobox automatic', 'آرایه زیستی', 'automatic', 'جعبه', 'taxobox'),
(208, 'جعبه آرایه زیستی', 'taxobox NULL', 'آرایه زیستی', 'NULL', 'جعبه', 'taxobox'),
(209, 'جعبه شخصیت‌های تالکین', 'infobox tolkien character', 'شخصیت‌های تالکین', 'tolkien character', 'جعبه', 'infobox'),
(210, 'جعبه اطلاعات نشریه', 'infobox newspaper', 'نشریه', 'newspaper', 'جعبه اطلاعات', 'infobox'),
(211, 'جعبه زیست', 'taxobox NULL', 'زیست', 'NULL', 'جعبه', 'taxobox'),
(212, 'جعبه شهر ایران', 'infobox settlement', 'شهر ایران', 'settlement', 'جعبه', 'infobox'),
(213, 'جعبه شهر ترکیه/آزمایشی', 'infobox settlement', 'شهر ترکیه/آزمایشی', 'settlement', 'جعبه', 'infobox'),
(214, 'جعبه اطلاعات رادیم', 'infobox radium', 'رادیم', 'radium', 'جعبه اطلاعات', 'infobox'),
(215, 'جعبه اطلاعات شهرهای سوئیس', 'infobox swiss town', 'شهرهای سوئیس', 'swiss town', 'جعبه اطلاعات', 'infobox'),
(216, 'جعبه اطلاعات شهرهای سوئیس', 'infobox former country', 'شهرهای سوئیس', 'former country', 'جعبه اطلاعات', 'infobox'),
(217, 'جعبه اطلاعات سرب', 'infobox lead', 'سرب', 'lead', 'جعبه اطلاعات', 'infobox'),
(218, 'جعبه اطلاعات شخصیت کمیک', 'infobox comics character and title', 'شخصیت کمیک', 'comics character and title', 'جعبه اطلاعات', 'infobox'),
(219, 'جعبه اطلاعات شخصیت کمیک', 'infobox comics character', 'شخصیت کمیک', 'comics character', 'جعبه اطلاعات', 'infobox'),
(220, 'جعبه فضاپیما', 'infobox spaceflight', 'فضاپیما', 'spaceflight', 'جعبه', 'infobox'),
(221, 'جعبه فضاپیما', 'infobox spacecraft class', 'فضاپیما', 'spacecraft class', 'جعبه', 'infobox'),
(222, 'جعبه اطلاعات ایتریم', 'infobox yttrium', 'ایتریم', 'yttrium', 'جعبه اطلاعات', 'infobox'),
(223, 'جعبه اطلاعات تانتال', 'infobox tantalum', 'تانتال', 'tantalum', 'جعبه اطلاعات', 'infobox'),
(224, 'جعبه اطلاعات اپیزود تلویزیون', 'infobox television episode', 'اپیزود تلویزیون', 'television episode', 'جعبه اطلاعات', 'infobox'),
(225, 'جعبه اطلاعات قبیله', 'infobox tribe', 'قبیله', 'tribe', 'جعبه اطلاعات', 'infobox'),
(226, 'جعبه اطلاعات سیستم‌عامل', 'infobox os', 'سیستم‌عامل', 'os', 'جعبه اطلاعات', 'infobox'),
(227, 'جعبه اطلاعات هلیم', 'infobox helium', 'هلیم', 'helium', 'جعبه اطلاعات', 'infobox'),
(228, 'جعبه هواپیما ۲', 'infobox aircraft begin', 'هواپیما ۲', 'aircraft begin', 'جعبه', 'infobox'),
(229, 'جعبه هواپیما ۲', 'infobox aircraft type', 'هواپیما ۲', 'aircraft type', 'جعبه', 'infobox'),
(230, 'جعبه اطلاعات شرکت', 'infobox company', 'شرکت', 'company', 'جعبه اطلاعات', 'infobox'),
(231, 'جعبه اطلاعات فرعون', 'infobox pharaoh', 'فرعون', 'pharaoh', 'جعبه اطلاعات', 'infobox'),
(232, 'جعبه اطلاعات فرعون', 'infobox pharaoh/serekh', 'فرعون', 'pharaoh/serekh', 'جعبه اطلاعات', 'infobox'),
(233, 'جعبه بیماری', 'infobox disease', 'بیماری', 'disease', 'جعبه', 'infobox'),
(234, 'جعبه اطلاعات نوبلیم', 'infobox nobelium', 'نوبلیم', 'nobelium', 'جعبه اطلاعات', 'infobox'),
(235, 'جعبه کانال تلویزیون', 'infobox television channel', 'کانال تلویزیون', 'television channel', 'جعبه', 'infobox'),
(236, 'جعبه اطلاعات دیسپروزیم', 'infobox dysprosium', 'دیسپروزیم', 'dysprosium', 'جعبه اطلاعات', 'infobox'),
(237, 'جعبه داور', 'infobox person', 'داور', 'person', 'جعبه', 'infobox'),
(238, 'جعبه اطلاعات آن‌ان‌بیوم', 'infobox copernicium', 'آن‌ان‌بیوم', 'copernicium', 'جعبه اطلاعات', 'infobox'),
(239, 'جعبه اطلاعات کریپتون', 'infobox krypton', 'کریپتون', 'krypton', 'جعبه اطلاعات', 'infobox'),
(240, 'جعبه اطلاعات زنون', 'infobox xenon', 'زنون', 'xenon', 'جعبه اطلاعات', 'infobox'),
(241, 'جعبه اطلاعات شهربازی', 'infobox amusement park', 'شهربازی', 'amusement park', 'جعبه اطلاعات', 'infobox'),
(242, 'جعبه بالای  کاربر', 'taxobox NULL', 'بالای  کاربر', 'NULL', 'جعبه', 'taxobox'),
(243, 'جعبه اطلاعات کانی', 'infobox mineral', 'کانی', 'mineral', 'جعبه اطلاعات', 'infobox'),
(244, 'جعبه میله', 'infobox country', 'میله', 'country', 'جعبه', 'infobox'),
(245, 'جعبه اطلاعات مولیبدن', 'infobox molybdenum', 'مولیبدن', 'molybdenum', 'جعبه اطلاعات', 'infobox'),
(246, 'جعبه اطلاعات صورت‌فلکی', 'infobox constellation', 'صورت‌فلکی', 'constellation', 'جعبه اطلاعات', 'infobox'),
(247, 'جعبه اطلاعات شهر یونان', 'infobox greek dimos', 'شهر یونان', 'greek dimos', 'جعبه اطلاعات', 'infobox'),
(248, 'جعبه ذرات بنیادی', 'infobox particle', 'ذرات بنیادی', 'particle', 'جعبه', 'infobox'),
(249, 'جعبه شهر عراق', 'infobox settlement', 'شهر عراق', 'settlement', 'جعبه', 'infobox'),
(250, 'جعبه اطلاعات معبد بودایی', 'infobox religious building', 'معبد بودایی', 'religious building', 'جعبه اطلاعات', 'infobox'),
(251, 'جعبه روستای ایران', 'infobox settlement', 'روستای ایران', 'settlement', 'جعبه', 'infobox'),
(252, 'جعبه اطلاعات کبالت', 'infobox cobalt', 'کبالت', 'cobalt', 'جعبه اطلاعات', 'infobox'),
(253, 'جعبه اطلاعات ژرمانیم', 'infobox germanium', 'ژرمانیم', 'germanium', 'جعبه اطلاعات', 'infobox'),
(254, 'جعبه اطلاعات قلع', 'infobox tin', 'قلع', 'tin', 'جعبه اطلاعات', 'infobox'),
(255, 'جعبه آب‌وهوا', 'infobox settlement', 'آب‌وهوا', 'settlement', 'جعبه', 'infobox'),
(256, 'جعبه تصویر  ستاره', 'starbox begin', 'تصویر  ستاره', 'begin', 'جعبه', 'starbox'),
(257, 'جعبه تصویر  ستاره', 'starbox character', 'تصویر  ستاره', 'character', 'جعبه', 'starbox'),
(258, 'جعبه تصویر  ستاره', 'starbox astrometry', 'تصویر  ستاره', 'astrometry', 'جعبه', 'starbox'),
(259, 'جعبه تصویر  ستاره', 'starbox detail', 'تصویر  ستاره', 'detail', 'جعبه', 'starbox'),
(260, 'جعبه تصویر  ستاره', 'starbox catalog', 'تصویر  ستاره', 'catalog', 'جعبه', 'starbox'),
(261, 'جعبه تصویر  ستاره', 'starbox reference', 'تصویر  ستاره', 'reference', 'جعبه', 'starbox'),
(262, 'جعبه تصویر  ستاره', 'starbox end', 'تصویر  ستاره', 'end', 'جعبه', 'starbox'),
(263, 'جعبه تصویر  ستاره', 'starbox observe', 'تصویر  ستاره', 'observe', 'جعبه', 'starbox'),
(264, 'جعبه تصویر  ستاره', 'starbox image', 'تصویر  ستاره', 'image', 'جعبه', 'starbox'),
(265, 'جعبه تصویر  ستاره', 'starbox observe 3s', 'تصویر  ستاره', 'observe 3s', 'جعبه', 'starbox'),
(266, 'جعبه اطلاعات لوتتیم', 'infobox lutetium', 'لوتتیم', 'lutetium', 'جعبه اطلاعات', 'infobox'),
(267, 'جعبه لشکرکشی‌های توکوگاوا ایه‌یاسو ‏', 'infobox military conflict', 'لشکرکشی‌های توکوگاوا ایه‌یاسو ‏', 'military conflict', 'جعبه', 'infobox'),
(268, 'جعبه اسید آمینه', 'chembox image', 'اسید آمینه', 'image', 'جعبه', 'chembox'),
(269, 'جعبه اسید آمینه', 'chembox properties', 'اسید آمینه', 'properties', 'جعبه', 'chembox'),
(270, 'جعبه اسید آمینه', 'chembox pharmacology', 'اسید آمینه', 'pharmacology', 'جعبه', 'chembox'),
(271, 'جعبه اسید آمینه', 'chembox hazards', 'اسید آمینه', 'hazards', 'جعبه', 'chembox'),
(272, 'جعبه اسید آمینه', 'chembox identifiers', 'اسید آمینه', 'identifiers', 'جعبه', 'chembox'),
(273, 'جعبه اسید آمینه', 'chembox NULL', 'اسید آمینه', 'NULL', 'جعبه', 'chembox'),
(274, 'جعبه اسید آمینه', 'chembox header', 'اسید آمینه', 'header', 'جعبه', 'chembox'),
(275, 'جعبه اسید آمینه', 'chembox structure', 'اسید آمینه', 'structure', 'جعبه', 'chembox'),
(276, 'جعبه اسید آمینه', 'chembox thermochemistry', 'اسید آمینه', 'thermochemistry', 'جعبه', 'chembox'),
(277, 'جعبه اسید آمینه', 'chembox related', 'اسید آمینه', 'related', 'جعبه', 'chembox'),
(278, 'جعبه اطلاعات ایتربیم', 'infobox ytterbium', 'ایتربیم', 'ytterbium', 'جعبه اطلاعات', 'infobox'),
(279, 'جعبه اطلاعات استرانسیم', 'infobox strontium', 'استرانسیم', 'strontium', 'جعبه اطلاعات', 'infobox'),
(280, 'جعبه اطلاعات آرایه زیستی', 'taxobox automatic', 'آرایه زیستی', 'automatic', 'جعبه اطلاعات', 'taxobox'),
(281, 'جعبه اطلاعات آرایه زیستی', 'taxobox NULL', 'آرایه زیستی', 'NULL', 'جعبه اطلاعات', 'taxobox'),
(282, 'جعبه اطلاعات ایالت آلمان', 'infobox german state', 'ایالت آلمان', 'german state', 'جعبه اطلاعات', 'infobox'),
(283, 'جعبه اطلاعات وانادیم', 'infobox vanadium', 'وانادیم', 'vanadium', 'جعبه اطلاعات', 'infobox'),
(284, 'جعبه اطلاعات زیرکونیم', 'infobox zirconium', 'زیرکونیم', 'zirconium', 'جعبه اطلاعات', 'infobox'),
(285, 'جعبه شهر لیبی', 'infobox settlement', 'شهر لیبی', 'settlement', 'جعبه', 'infobox'),
(286, 'جعبه استان ایران', 'infobox settlement', 'استان ایران', 'settlement', 'جعبه', 'infobox'),
(287, 'جعبه اطلاعات کوریم', 'infobox curium', 'کوریم', 'curium', 'جعبه اطلاعات', 'infobox'),
(288, 'جعبه اطلاعات انیمانگا/پا', 'infobox animanga/header', 'انیمانگا/پا', 'animanga/header', 'جعبه اطلاعات', 'infobox'),
(289, 'جعبه اطلاعات انیمانگا/پا', 'infobox animanga/video', 'انیمانگا/پا', 'animanga/video', 'جعبه اطلاعات', 'infobox'),
(290, 'جعبه اطلاعات انیمانگا/پا', 'infobox animanga/footer', 'انیمانگا/پا', 'animanga/footer', 'جعبه اطلاعات', 'infobox'),
(291, 'جعبه اطلاعات انیمانگا/پا', 'infobox animanga/print', 'انیمانگا/پا', 'animanga/print', 'جعبه اطلاعات', 'infobox'),
(292, 'جعبه اطلاعات انیمانگا/پا', 'infobox animanga/other', 'انیمانگا/پا', 'animanga/other', 'جعبه اطلاعات', 'infobox'),
(293, 'جعبه اطلاعات انیمانگا/پا', 'infobox animanga/game', 'انیمانگا/پا', 'animanga/game', 'جعبه اطلاعات', 'infobox'),
(294, 'جعبه اطلاعات لارنسیم', 'infobox lawrencium', 'لارنسیم', 'lawrencium', 'جعبه اطلاعات', 'infobox'),
(295, 'جعبه اطلاعات رونتگنیم', 'infobox roentgenium', 'رونتگنیم', 'roentgenium', 'جعبه اطلاعات', 'infobox'),
(296, 'جعبه اطلاعات اصطلاح پزشکی', 'infobox birth control', 'اصطلاح پزشکی', 'birth control', 'جعبه اطلاعات', 'infobox'),
(297, 'جعبه مشخصات  ستاره', 'starbox begin', 'مشخصات  ستاره', 'begin', 'جعبه', 'starbox'),
(298, 'جعبه مشخصات  ستاره', 'starbox image', 'مشخصات  ستاره', 'image', 'جعبه', 'starbox'),
(299, 'جعبه مشخصات  ستاره', 'starbox character', 'مشخصات  ستاره', 'character', 'جعبه', 'starbox'),
(300, 'جعبه مشخصات  ستاره', 'starbox astrometry', 'مشخصات  ستاره', 'astrometry', 'جعبه', 'starbox'),
(301, 'جعبه مشخصات  ستاره', 'starbox catalog', 'مشخصات  ستاره', 'catalog', 'جعبه', 'starbox'),
(302, 'جعبه مشخصات  ستاره', 'starbox reference', 'مشخصات  ستاره', 'reference', 'جعبه', 'starbox'),
(303, 'جعبه مشخصات  ستاره', 'starbox end', 'مشخصات  ستاره', 'end', 'جعبه', 'starbox'),
(304, 'جعبه مشخصات  ستاره', 'starbox observe 3s', 'مشخصات  ستاره', 'observe 3s', 'جعبه', 'starbox'),
(305, 'جعبه مشخصات  ستاره', 'starbox detail', 'مشخصات  ستاره', 'detail', 'جعبه', 'starbox'),
(306, 'جعبه مشخصات  ستاره', 'starbox observe', 'مشخصات  ستاره', 'observe', 'جعبه', 'starbox'),
(307, 'جعبه جنگ‌افزار', 'infobox weapon', 'جنگ‌افزار', 'weapon', 'جعبه', 'infobox'),
(308, 'جعبه اطلاعات سفینه فضایی', 'infobox spaceflight', 'سفینه فضایی', 'spaceflight', 'جعبه اطلاعات', 'infobox');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `wikipedia_extracted_template_mapping`
--
ALTER TABLE `wikipedia_extracted_template_mapping`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `wikipedia_extracted_template_mapping`
--
ALTER TABLE `wikipedia_extracted_template_mapping`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=309;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;